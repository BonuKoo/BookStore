package com.bookService.core.domain.stock;

import com.bookService.core.domain.item.Item;
import com.bookService.core.domain.item.repository.ItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 오버셀링 잔여 리스크의 실증: 결제 확정(SUCCESS)과 재고 확정이 분리된 구조에서
 * 마지막 재고를 두고 결제가 동시에 확정되면 "결제는 성공했는데 재고가 없는" 주문이
 * 생긴다. 이 테스트는 그 상태가 실제로 만들어짐과, 그 주문을 DB만으로
 * (SUCCESS && stock_deduction 기록 없음) 정확히 식별할 수 있음을 계약으로 고정한다.
 * — 이 식별 쿼리가 향후 자동 환불 컨슈머의 입력 정의다.
 *
 * 동시성 자체(원자 UPDATE의 음수 차단)는 StockDeductionServiceIntegrationTest가
 * 이미 검증하므로, 여기서는 결정적(순차) 재현으로 식별 계약만 본다.
 */
@SpringBootTest
class OversellRefundTargetIntegrationTest {

    private static final String ISBN_PREFIX = "ovtest-isbn-";
    private static final String ORDER_PREFIX = "ovtest-order-";
    private static final long ID_BASE = 30_000_000L; // lt-(1천만)/ov-(2천만) 대역과 분리
    private static final int AMOUNT = 1000;
    private static final long BUYER = 1L; // user_entity 실존 행 (seed_orders.py와 동일 전제)

    @Autowired private StockDeductionService stockDeductionService;
    @Autowired private ItemRepository itemRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM stock_deduction WHERE order_id LIKE ?", ORDER_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM payment_orders WHERE order_id LIKE ?", ORDER_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM payment_event WHERE order_id LIKE ?", ORDER_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM item WHERE isbn LIKE ?", ISBN_PREFIX + "%");
    }

    /**
     * 결제가 confirm 경로를 통과한 뒤의 상태를 DB에 재현한다.
     * 중요: 실제 confirm 경로는 payment_ORDERS.payment_status만 SUCCESS로 갱신하고
     * payment_event.payment_status는 NOT_STARTED로 남긴다(완결은 is_payment_done로 추적).
     * 따라서 환불 대상 식별도 payment_orders 기준이어야 한다 — payment_event를
     * SUCCESS로 심는 비현실적 픽스처를 쓰지 않도록 여기서도 NOT_STARTED로 둔다.
     */
    private void seedConfirmedOrder(long id, String orderId, String isbn) {
        jdbcTemplate.update(
                "INSERT INTO payment_event (payment_event_id, buyer, buyer_id, is_payment_done, "
                        + "order_id, order_name, payment_status, version, created_at) "
                        + "VALUES (?, ?, ?, 0, ?, 'oversell-test', 'NOT_STARTED', 0, NOW(6))",
                id, BUYER, BUYER, orderId);
        jdbcTemplate.update(
                "INSERT INTO payment_orders (payment_order_id, amount, failed_count, "
                        + "is_ledger_updated, is_wallet_updated, order_id, payment_status, "
                        + "product_id, seller_id, payment_id, quantity, threshold, created_at) "
                        + "VALUES (?, ?, 0, 0, 0, ?, 'SUCCESS', ?, 999, ?, 1, 0, NOW(6))",
                id, AMOUNT, orderId, isbn, id);
    }

    @Test
    @DisplayName("오버셀링: 결제 2건 확정·재고 1개 → 차감 실패 주문이 '환불 대상'으로 정확히 식별된다")
    void oversell_confirmedPaymentWithoutStock_isIdentifiedAsRefundTarget() {
        String isbn = ISBN_PREFIX + System.currentTimeMillis();
        itemRepository.save(new Item(isbn, "오버셀링 테스트 도서", AMOUNT, 1)); // 마지막 재고 1개

        String winner = ORDER_PREFIX + "winner";
        String loser = ORDER_PREFIX + "loser";
        seedConfirmedOrder(ID_BASE, winner, isbn);
        seedConfirmedOrder(ID_BASE + 1, loser, isbn);

        List<Map<String, Object>> items = List.of(Map.of("productId", isbn, "quantity", 1));

        // 결제는 둘 다 이미 SUCCESS — 재고 차감에서만 승패가 갈린다
        stockDeductionService.deduct(winner, items);
        assertThatThrownBy(() -> stockDeductionService.deduct(loser, items))
                .isInstanceOf(InsufficientStockException.class);

        // 재고는 정확히 소진, 음수 없음
        Integer stock = jdbcTemplate.queryForObject(
                "SELECT stock_quantity FROM item WHERE isbn = ?", Integer.class, isbn);
        assertThat(stock).isZero();

        // 환불 대상 식별 계약: payment_orders.payment_status=SUCCESS && stock_deduction 없음
        // → 정확히 loser 1건, 환불액까지 산출 (payment_event가 아니라 payment_orders 기준)
        List<Map<String, Object>> refundTargets = jdbcTemplate.queryForList(
                "SELECT po.order_id, SUM(po.amount) AS refund_amount "
                        + "FROM payment_orders po "
                        + "LEFT JOIN stock_deduction sd ON sd.order_id = po.order_id "
                        + "WHERE po.order_id LIKE ? AND po.payment_status = 'SUCCESS' "
                        + "AND sd.order_id IS NULL "
                        + "GROUP BY po.order_id",
                ORDER_PREFIX + "%");

        assertThat(refundTargets).hasSize(1);
        assertThat(refundTargets.getFirst().get("order_id")).isEqualTo(loser);
        assertThat(((Number) refundTargets.getFirst().get("refund_amount")).intValue())
                .isEqualTo(AMOUNT);
    }
}
