package com.bookService.core.domain.stock;

import com.bookService.core.domain.item.Item;
import com.bookService.core.domain.item.repository.ItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 재고 차감의 동시성 계약 검증. 실제 MySQL에서 스레드별 독립 트랜잭션으로 실행해
 * "원자적 조건부 UPDATE + UNIQUE 멱등 가드"가 실제 경쟁 상황에서 지켜지는지 본다.
 */
@SpringBootTest
class StockDeductionServiceIntegrationTest {

    private static final String ISBN_PREFIX = "stock-test-";
    private static final String ORDER_PREFIX = "stock-test-order-";

    @Autowired private StockDeductionService stockDeductionService;
    @Autowired private ItemRepository itemRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM stock_deduction WHERE order_id LIKE ?", ORDER_PREFIX + "%");
        jdbcTemplate.update("DELETE FROM item WHERE isbn LIKE ?", ISBN_PREFIX + "%");
    }

    private String createItem(int stock) {
        String isbn = ISBN_PREFIX + UUID.randomUUID();
        itemRepository.save(new Item(isbn, "동시성 테스트 도서", 10000, stock));
        return isbn;
    }

    private int currentStock(String isbn) {
        return jdbcTemplate.queryForObject(
                "SELECT stock_quantity FROM item WHERE isbn = ?", Integer.class, isbn);
    }

    @Test
    @DisplayName("정상 차감: 주문 항목만큼 재고가 줄고 멱등 기록이 남는다")
    void deduct_success() {
        String isbn = createItem(10);
        String orderId = ORDER_PREFIX + UUID.randomUUID();

        stockDeductionService.deduct(orderId, List.of(Map.of("productId", isbn, "quantity", 3)));

        assertThat(currentStock(isbn)).isEqualTo(7);
        assertThat(stockDeductionService.alreadyProcessed(orderId)).isTrue();
    }

    @Test
    @DisplayName("멱등성: 같은 주문을 두 번 처리하면 두 번째는 거부되고 재고는 1회만 차감된다")
    void deduct_duplicateOrder_deductsOnlyOnce() {
        String isbn = createItem(10);
        String orderId = ORDER_PREFIX + UUID.randomUUID();
        List<Map<String, Object>> items = List.of(Map.of("productId", isbn, "quantity", 3));

        stockDeductionService.deduct(orderId, items);
        assertThatThrownBy(() -> stockDeductionService.deduct(orderId, items))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(currentStock(isbn)).isEqualTo(7); // 4가 아니라 7
    }

    @Test
    @DisplayName("all-or-nothing: 한 항목이 재고 부족이면 전체 롤백 — 다른 항목 차감도 멱등 기록도 남지 않는다")
    void deduct_insufficientStock_rollsBackWholeOrder() {
        String isbnEnough = createItem(10);
        String isbnShort = createItem(1);
        String orderId = ORDER_PREFIX + UUID.randomUUID();

        assertThatThrownBy(() -> stockDeductionService.deduct(orderId, List.of(
                Map.of("productId", isbnEnough, "quantity", 2),
                Map.of("productId", isbnShort, "quantity", 5)
        ))).isInstanceOf(InsufficientStockException.class);

        assertThat(currentStock(isbnEnough)).isEqualTo(10); // 롤백으로 원상복구
        assertThat(currentStock(isbnShort)).isEqualTo(1);
        assertThat(stockDeductionService.alreadyProcessed(orderId)).isFalse();
    }

    @Test
    @DisplayName("동시성/lost update: 서로 다른 주문 20개가 같은 상품을 동시에 차감해도 총합이 정확하다")
    void deduct_concurrentOrders_noLostUpdate() throws InterruptedException {
        String isbn = createItem(100);
        int threads = 20;

        runConcurrently(threads, i ->
                stockDeductionService.deduct(ORDER_PREFIX + "c-" + i + "-" + UUID.randomUUID(),
                        List.of(Map.of("productId", isbn, "quantity", 2))));

        // 읽고-더하고-쓰기였다면 갱신 유실로 100 - 40보다 큰 값이 남는다
        assertThat(currentStock(isbn)).isEqualTo(100 - threads * 2);
    }

    @Test
    @DisplayName("동시성/음수 차단: 재고 5에 동시 주문 20개(각 1개) → 정확히 5개만 성공하고 재고는 0")
    void deduct_concurrentOverSell_neverGoesNegative() throws InterruptedException {
        String isbn = createItem(5);
        int threads = 20;
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger insufficient = new AtomicInteger();

        runConcurrently(threads, i -> {
            try {
                stockDeductionService.deduct(ORDER_PREFIX + "o-" + i + "-" + UUID.randomUUID(),
                        List.of(Map.of("productId", isbn, "quantity", 1)));
                succeeded.incrementAndGet();
            } catch (InsufficientStockException e) {
                insufficient.incrementAndGet();
            }
        });

        assertThat(succeeded.get()).isEqualTo(5);
        assertThat(insufficient.get()).isEqualTo(15);
        assertThat(currentStock(isbn)).isEqualTo(0);
    }

    private void runConcurrently(int threads, java.util.function.IntConsumer task) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final int idx = i;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    task.accept(idx);
                } catch (Exception ignored) {
                    // 성공/실패 집계는 task 안에서 수행
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        start.countDown(); // 전 스레드 동시 출발
        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();
    }
}
