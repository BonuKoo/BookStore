package com.bookService.core.facade;

import com.bookService.core.domain.item.Item;
import com.bookService.core.domain.item.repository.ItemRepository;
import com.bookService.core.domain.item.service.ItemService;
import com.bookService.core.test.item.exception.StockUnderflowException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

// thread 5~10 정도에서 안정적 반응
@SpringBootTest
class OptimisticLockStockFacadeTest {

    private ExecutorService executor;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OptimisticLockStockFacade stockFacade;

    String isbn = "ISBN0000521";
    private final int INITIAL_STOCK = 200; // 테스트를 위해 넉넉하게 설정
    private final int MAX_CONCURRENT_REQUESTS = 200;

    private final AtomicInteger systemFailureCount = new AtomicInteger(0); // 락 충돌(500 에러)
    private final AtomicInteger bizFailureCount = new AtomicInteger(0);    // 재고 부족(422 에러)

    @BeforeEach
    void setup() {

        Item item = itemRepository.findByIsbn(isbn).get();
        item.setStockQuantity(INITIAL_STOCK);
        itemRepository.save(item);

        systemFailureCount.set(0); // 실패 카운트 초기화
        bizFailureCount.set(0);
        System.out.println("--- 테스트 시작: 동시 요청 수 " + MAX_CONCURRENT_REQUESTS + " ---");
    }

    /*
   @AfterEach
    void increase(){
   }
*/
    @Test
    void decrease_with_optimistic_lock_and_try() throws InterruptedException {
        // CountDownLatch는 필요하지 않음. CompletableFuture.allOf().join()이 대체합니다.
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_REQUESTS);
        long startTime = System.currentTimeMillis();

        // 1. CompletableFuture 리스트 생성 및 @Async 호출
        CompletableFuture<Void>[] futures = IntStream.range(0, MAX_CONCURRENT_REQUESTS)
                // 각 요청은 1개씩 재고를 감소시킵니다.
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    // Facade의 @Async 메서드를 호출합니다.
                    return stockFacade.decreaseAsync(isbn, 1);
                }, executorService).thenCompose(cf -> cf)) // 비동기 작업의 결과를 다시 CompletableFuture로 연결
                .toArray(CompletableFuture[]::new);


// 2. 모든 비동기 작업의 완료 대기 (재시도 로직 포함)
        try {
            CompletableFuture.allOf(futures).join();
        } catch (Exception e) {
            // 모든 개별 Future를 순회하며 예외 유형 확인
            for (CompletableFuture<Void> future : futures) {
                if (future.isCompletedExceptionally()) {
                    try {
                        // 예외 발생 시 future.get() 호출로 실제 예외를 던지게 함
                        future.get();
                    } catch (Exception fe) {
                        // fe.getCause()가 failedFuture로 반환된 실제 예외
                        Throwable rootCause = fe.getCause();

                        // 1. 시스템 오류: 락 충돌 (500 에러)
                        if (rootCause instanceof RuntimeException && rootCause.getMessage().contains("현재 서버 부하로 결제 실패")) {
                            systemFailureCount.incrementAndGet();
                            System.err.println("시스템 오류 (락 충돌): " + rootCause.getMessage());
                        }
                        // 2. 비즈니스 오류: 재고 부족 (422 에러)
                        // StockUnderflowException 클래스가 정의되어 있고, Facade에서 반환되어야 함.
                        else if (rootCause instanceof StockUnderflowException) {
                            bizFailureCount.incrementAndGet();
                            System.err.println("비즈니스 오류 (재고 부족): " + rootCause.getMessage());
                        }
                        // 3. 기타 시스템 실패
                        else {
                            // 예상치 못한 오류는 시스템 실패로 간주
                            systemFailureCount.incrementAndGet();
                            System.err.println("기타 시스템 오류: " + rootCause.getMessage());
                        }
                    }
                }
            }
        } finally {
            executorService.shutdown();
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("총 소요 시간: " + duration + "ms");


        // 3. 검증
        Item finalItem = itemRepository.findByIsbn(isbn)
                .orElseThrow(() -> new AssertionError("Item not found after test"));

        // 최종 실패 건수는 시스템 오류와 비즈니스 오류를 모두 합산해야 함.
        int totalFailureCount = systemFailureCount.get() + bizFailureCount.get();
        int successfulRequests = MAX_CONCURRENT_REQUESTS - totalFailureCount;
        int expectedStock = INITIAL_STOCK - successfulRequests;

        System.out.println("최종 재고: " + finalItem.getStockQuantity());
        System.out.println("시스템 오류 (락 충돌) 수: " + systemFailureCount.get());
        System.out.println("비즈니스 오류 (재고 부족) 수: " + bizFailureCount.get());

        // 1. 시스템 오류 (락 충돌) 검증: 0이어야 안정적입니다.
        assertThat(systemFailureCount.get())
                .as("시스템 오류(락 충돌 500 에러)는 발생하지 않아야 합니다.")
                .isEqualTo(0);

        // 2. 재고 정합성 검증: 총 성공 요청 수로 계산한 예상 재고와 일치해야 합니다.
        assertThat(finalItem.getStockQuantity())
                .as("최종 재고가 예상치와 일치하는가? (초기 - 총 성공 요청 수)")
                .isEqualTo(expectedStock);
    }

}