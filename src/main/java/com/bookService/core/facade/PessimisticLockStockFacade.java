package com.bookService.core.facade;

import com.bookService.core.test.item.OptimisticLockItemService;
import com.bookService.core.test.item.PessimisticLockItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionSystemException;

@Component
@RequiredArgsConstructor
public class PessimisticLockStockFacade {

    private final PessimisticLockItemService pessimisticLockItemService;

    /**
     * 낙관적 락을 이용한 재고 감소 및 재시도 로직.
     * Checkout 시스템에 연동될 경우, 사용자 경험을 위해 재시도 횟수와 딜레이를 짧게 튜닝함.
     *
     * @param isbn 재고를 감소시킬 아이템의 ISBN
     * @param quantity 감소시킬 수량
     * @throws InterruptedException Thread.sleep으로 인한 예외
     */
    public void decrease1(String isbn, int quantity) throws InterruptedException {
        // [사용자 경험 튜닝] 최대 재시도 횟수를 5회로 축소 (100회 -> 5회)
        // 5회 재시도 후에도 실패하면, 빠르게 사용자에게 실패 응답을 전달
        int maxRetries = 5;

        // 초기 지연 시간 (10ms)
        long initialDelay = 10;

        // 지수 백오프 딜레이의 최대값을 100ms로 제한 (1000ms -> 100ms)
        // 최대 누적 대기 시간을 250ms 이내로 유지하여 1초 이내 응답을 목표
        final long MAX_BACKOFF_DELAY_MS = 100L;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                // @Transactional(REQUIRES_NEW)가 적용된 Service 메서드 호출
                pessimisticLockItemService.decrease(isbn, quantity);
                return; // 성공 시 루프 종료
            } catch (ObjectOptimisticLockingFailureException | TransactionSystemException e) {
                // 락 충돌 (Optimistic Lock Conflict) 발생 시

                if (attempt >= maxRetries - 1) {
                    // 최대 재시도 횟수 초과 시, 최종 실패 처리
//                    System.err.println("thread Fail :: Max retries reached for ISBN: " + isbn);
                    // Checkout 실패 시, 사용자에게 명확한 메시지를 전달할 수 있도록 RuntimeException 발생
                    throw new RuntimeException("현재 서버 부하로 결제 실패. 잠시 후 다시 시도해 주세요.", e);
                }

                // 지수 백오프 (Exponential Backoff) 계산
                long calculatedDelay = initialDelay * (1L << attempt);

                // 최대 딜레이를 초과하지 않도록 캡(Cap) 적용
                long delay = Math.min(calculatedDelay, MAX_BACKOFF_DELAY_MS);

//                System.out.printf("thread Conflict :: Retrying (Attempt %d, Delay %dms) for ISBN: %s%n", attempt + 1, delay, isbn);
                Thread.sleep(delay);

            } catch (Exception e) {
                // 재고 부족(StockUnderflowException) 등 복구 불가능한 비즈니스 예외
//                System.err.println("thread Fail :: Permanent error encountered for ISBN: " + isbn + ". Rolling back. Error: " + e.getMessage());
                throw new RuntimeException("재고 감소 작업 중 복구 불가능한 비즈니스 실패", e);
            }
        }
    }

    public void increase1(String isbn, int quantity) throws InterruptedException {
        // [사용자 경험 튜닝] 최대 재시도 횟수를 5회로 축소 (100회 -> 5회)
        // 5회 재시도 후에도 실패하면, 빠르게 사용자에게 실패 응답을 전달
        int maxRetries = 50;

        // 초기 지연 시간 (10ms)
        long initialDelay = 10;

        // 지수 백오프 딜레이의 최대값을 100ms로 제한 (1000ms -> 100ms)
        // 최대 누적 대기 시간을 250ms 이내로 유지하여 1초 이내 응답을 목표
        final long MAX_BACKOFF_DELAY_MS = 1000L;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                // @Transactional(REQUIRES_NEW)가 적용된 Service 메서드 호출
                pessimisticLockItemService.increase_1(isbn, quantity);
                return; // 성공 시 루프 종료
            } catch (ObjectOptimisticLockingFailureException | TransactionSystemException e) {
                // 락 충돌 (Optimistic Lock Conflict) 발생 시

                if (attempt >= maxRetries - 1) {
                    // 최대 재시도 횟수 초과 시, 최종 실패 처리
//                    System.err.println("thread Fail :: Max retries reached for ISBN: " + isbn);
                    // Checkout 실패 시, 사용자에게 명확한 메시지를 전달할 수 있도록 RuntimeException 발생
                    throw new RuntimeException("현재 서버 부하로 결제 실패. 잠시 후 다시 시도해 주세요.", e);
                }

                // 지수 백오프 (Exponential Backoff) 계산
                long calculatedDelay = initialDelay * (1L << attempt);

                // 최대 딜레이를 초과하지 않도록 캡(Cap) 적용
                long delay = Math.min(calculatedDelay, MAX_BACKOFF_DELAY_MS);

//                System.out.printf("thread Conflict :: Retrying (Attempt %d, Delay %dms) for ISBN: %s%n", attempt + 1, delay, isbn);
                Thread.sleep(delay);

            } catch (Exception e) {
                // 재고 부족(StockUnderflowException) 등 복구 불가능한 비즈니스 예외
//                System.err.println("thread Fail :: Permanent error encountered for ISBN: " + isbn + ". Rolling back. Error: " + e.getMessage());
                throw new RuntimeException("재고 감소 작업 중 복구 불가능한 비즈니스 실패", e);
            }
        }
    }
    /*
    public void decrease2(String isbn, int quantity) throws InterruptedException {
        int maxRetries = 5;
        long initialDelay = 10;
        final long MAX_BACKOFF_DELAY_MS = 100L;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                pessimisticLockItemService.decrease2(isbn, quantity);
                return;
            } catch (ObjectOptimisticLockingFailureException | TransactionSystemException e) {
                if (attempt >= maxRetries - 1) {
                    throw new RuntimeException("현재 서버 부하로 결제 실패. 잠시 후 다시 시도해 주세요.", e);
                }
                long calculatedDelay = initialDelay * (1L << attempt);
                long delay = Math.min(calculatedDelay, MAX_BACKOFF_DELAY_MS);
                Thread.sleep(delay);

            } catch (Exception e) {
                throw new RuntimeException("재고 감소 작업 중 복구 불가능한 비즈니스 실패", e);
            }
        }
    }
*/


}
