package com.bookService.core.domain.stock;

import com.bookService.core.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * payment.confirmed 이벤트 기반 재고 차감.
 *
 * 동시성 처리 3계층:
 *  1) 멱등성 — stock_deduction(order_id UNIQUE)에 saveAndFlush를 먼저 실행.
 *     같은 주문이 중복 전달되면(at-least-once) 제약 위반으로 즉시 실패 → 이중 차감 차단.
 *     동시에 같은 주문을 두 스레드가 처리해도 한쪽만 커밋된다.
 *  2) Lost update 차단 — deductStock은 "UPDATE ... SET stock = stock - ? WHERE stock >= ?"
 *     원자 연산이라 읽고-더하는 경쟁 자체가 없다.
 *  3) 주문 단위 all-or-nothing — 한 항목이라도 재고 부족이면 예외로 전체 롤백
 *     (이미 차감된 다른 항목 + 멱등 기록까지 되돌림).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockDeductionService {

    private final StockDeductionRepository stockDeductionRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public void deduct(String orderId, List<Map<String, Object>> items) {
        // 멱등 가드. 중복이면 여기서 DataIntegrityViolationException — 호출측(리스너)에서 구분 처리.
        stockDeductionRepository.saveAndFlush(StockDeduction.builder()
                .orderId(orderId)
                .processedAt(LocalDateTime.now())
                .build());

        for (Map<String, Object> item : items) {
            String isbn = String.valueOf(item.get("productId"));
            int quantity = ((Number) item.get("quantity")).intValue();

            int updated = itemRepository.deductStock(isbn, quantity);
            if (updated == 0) {
                throw new InsufficientStockException(
                        "재고 부족: orderId=" + orderId + ", isbn=" + isbn + ", 요청수량=" + quantity);
            }
            log.info("재고 차감 완료: orderId={}, isbn={}, quantity={}", orderId, isbn, quantity);
        }
    }

    @Transactional(readOnly = true)
    public boolean alreadyProcessed(String orderId) {
        return stockDeductionRepository.existsByOrderId(orderId);
    }
}
