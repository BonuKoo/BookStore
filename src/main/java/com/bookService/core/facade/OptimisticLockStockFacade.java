package com.bookService.core.facade;

import com.bookService.core.test.item.OptimisticLockItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionSystemException;

@Component
@RequiredArgsConstructor
public class OptimisticLockStockFacade {

    private final OptimisticLockItemService optimisticLockItemService;

    public void decrease(String isbn, int quantity) throws InterruptedException {
        int maxRetries = 10;
        int initialDelay = 10; // 초기 대기 시간 (10ms)
        // [신규 추가] 지수 백오프 딜레이의 최대값을 1초(1000ms)로 제한합니다.
        final long MAX_BACKOFF_DELAY_MS = 1000L;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                // @Transactional(REQUIRES_NEW)가 적용된 Service 메서드 호출
                // quantity는 Facade에서 Long으로 받지만 Service에서는 int로 처리한다고 가정하고 intValue() 사용
                optimisticLockItemService.decrease(isbn, quantity);
                return; // 성공 시 루프 종료
            } catch (ObjectOptimisticLockingFailureException | TransactionSystemException e) {
                // ObjectOptimisticLockingFailureException: 일반적인 락 충돌
                // TransactionSystemException: Commit 실패 시 Spring이 던지는 래퍼 예외 (내부에 락 충돌 포함 가능)

                if (attempt >= maxRetries - 1) {
                    // 최대 재시도 횟수 초과 시, 충돌 예외를 던지고 최종 실패 처리
                    System.err.println("thread Fail :: Max retries reached for ISBN: " + isbn);
                    throw new RuntimeException("최대 재시도 횟수 초과로 재고 감소 실패", e);
                }

                // Exponential Backoff 계산 (10ms, 20ms, 40ms, 80ms, 160ms ...)
                long calculatedDelay = initialDelay * (1L << attempt);

                // [수정] 계산된 딜레이가 MAX_BACKOFF_DELAY_MS를 초과하지 않도록 캡(Cap)을 적용합니다.
                long delay = Math.min(calculatedDelay, MAX_BACKOFF_DELAY_MS);

                System.out.printf("thread Conflict :: Retrying (Attempt %d, Delay %dms) for ISBN: %s%n", attempt + 1, delay, isbn);
                Thread.sleep(delay);

            } catch (Exception e) {
                // StockUnderflowException 등 복구 불가능한 비즈니스 예외
                // 재시도하지 않고 즉시 던져서 상위 스레드를 종료시킵니다.
                System.err.println("thread Fail :: Permanent error encountered for ISBN: " + isbn + ". Rolling back. Error: " + e.getMessage());
                throw new RuntimeException("비즈니스 로직 실패로 재고 감소 작업 실패", e);
            }
        }
    }
}