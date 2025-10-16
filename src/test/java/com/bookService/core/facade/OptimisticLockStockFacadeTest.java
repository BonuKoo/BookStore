package com.bookService.core.facade;

import com.bookService.core.domain.item.Item;
import com.bookService.core.domain.item.repository.ItemRepository;
import com.bookService.core.domain.item.service.ItemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OptimisticLockStockFacadeTest {

   @Autowired
   private ItemService itemService;

   @Autowired
   private ItemRepository itemRepository;

   @Autowired
   private OptimisticLockStockFacade optimisticLockStockFacade;

   String isbn = "ISBN0000002";


   @BeforeEach
    void setup(){
       Optional<Item> isbn0000002 = itemRepository.findByIsbn(isbn);
       Item item = isbn0000002.get();
       item.setStockQuantity(30);
//       itemRepository.save(item);
   }
    /*
   @AfterEach
    void increase(){
   }*/

   @Test
   void decrease_with_optimistic_lock_and_try()throws InterruptedException{
       Optional<Item> isbn0000002 = itemRepository.findByIsbn(isbn);
       Item item = isbn0000002.get();

        int threadCount = 10;

       ExecutorService executorService = Executors.newFixedThreadPool(32);
       CountDownLatch latch = new CountDownLatch(threadCount);

       for (int i = 0; i<threadCount; i++){
           executorService.submit(()->{
               try {
                   optimisticLockStockFacade.decrease(item.getIsbn(),1);
               } catch (InterruptedException e){
                   Thread.currentThread().interrupt();
               } catch (Exception e){
                   System.err.println("thread Fail :: " + e.getMessage());
               } finally {
                   latch.countDown();
               }
           });
       }
       // 스레드가 작업을 완료할 때까지 대기
       latch.await();
       executorService.shutdown();

       Item finalItem = itemRepository.findById(isbn)
               .orElseThrow(() -> new AssertionError("Item not found after test"));

       // 검증 최종 재고는 0이어야 한다.
       assertThat(finalItem.getStockQuantity())
               .as("최종 재고는 0인가?")
               .isEqualTo(0);

       //검증 최종 버전은 최소 100 이상 증가
       System.out.println("Final Item Version:"+finalItem.getVersion());
       assertThat(finalItem.getStockQuantity())
               .as("최종 버전은 100이상인가")
               .isGreaterThanOrEqualTo(30);

   }

}