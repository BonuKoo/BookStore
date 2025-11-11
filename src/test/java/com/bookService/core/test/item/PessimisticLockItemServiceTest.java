package com.bookService.core.test.item;

import com.bookService.core.domain.item.Item;
import com.bookService.core.domain.item.repository.ItemRepository;
import com.bookService.core.domain.item.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

//@SpringBootTest
class PessimisticLockItemServiceTest {
    /*
    @Autowired
    private PessimisticLockItemService pessimisticLockItemService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    String isbn = "ISBN0000002";

    @BeforeEach
    void setup() {
        // 1. 기존 Item을 ISBN으로 조회합니다.
        Optional<Item> optionalItem = itemRepository.findByIsbn(isbn);
        if (optionalItem.isPresent()) {
            // 2. Item이 존재하면 재고를 100으로 설정하고 저장합니다 (UPDATE).
            Item item = optionalItem.get();
            item.setStockQuantity(100);
            itemRepository.saveAndFlush(item);
        } else {
            // 3. Item이 존재하지 않으면, 테스트를 위해 새로운 Item을 생성하고 저장합니다 (INSERT).
            // 이 코드는 Item 엔티티의 생성자가 필요하지만, 테스트 환경 안정성을 위해 임시로 주석 처리
            // System.out.println("WARN: Item with ISBN " + isbn + " not found. Skipping stock setup.");
            // 실제 환경에서는 Item 생성 로직이 필요합니다.
        }
    }

    @Test
    @DisplayName("비관적 락 사용 시 100개의 요청이 동시에 재고를 1씩 감소시켜도 최종 재고는 0이다.")
    public void concurrent_stock_decrease_with_pessimistic_lock() throws InterruptedException {
        // given
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 비관적 락을 통해 동시성 문제를 해결하고, 재고가 0이 될 때까지는 정상적으로 처리됩니다.
                    pessimisticLockItemService.decrease(isbn, 1);
                } catch (RuntimeException e) {
                    // [수정] 3. 재고 부족 시 발생하는 예상된 RuntimeException을 처리합니다.
                    // (Item 엔티티의 removeStock에서 던지는 예외)
                    // 예외 메시지: "재고는 0개 미만이 될 수 없습니다."
                    if (!e.getMessage().contains("재고는 0개 미만")) {
                        // 예상치 못한 다른 예외가 발생하면 출력합니다.
                        System.err.println("Unexpected RuntimeException: " + e.getMessage());
                    }
                } catch (Exception e) {
                    // 기타 예외 처리 (e.g., Item not found)
                    System.err.println("Unexpected exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드가 종료될 때까지 대기

        // then
        // @Transactional 내에서 findById를 호출하여 변경된 상태를 확인합니다.
        Item finalItem = itemRepository.findById(isbn)
                .orElseThrow(() -> new RuntimeException("Test Item not found"));

        // 비관적 락이 동시성을 보장하여 30번의 감소만 성공했고, 최종 재고는 0입니다.
        assertThat(finalItem.getStockQuantity()).isEqualTo(0);
    }*/
}
