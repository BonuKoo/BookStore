package com.myboard.toy.application.item.threadTest;

import com.myboard.toy.redis.facade.LettuceLockStockFacade;
import com.myboard.toy.sales.cartitem.repository.CartItemRepository;
import com.myboard.toy.sales.domain.entity.Item;
import com.myboard.toy.sales.item.repository.ItemRepository;
import com.myboard.toy.sales.item.service.ItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ItemServiceTest {



    @Autowired
    private LettuceLockStockFacade lettuceLockStockFacade;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @MockBean
    private CartItemRepository cartItemRepository;

    @BeforeEach
    public void before(){
        itemRepository.saveAndFlush(new Item("12341234","한강 굴다리",12345,100));
    }

    @AfterEach
    public void after(){
        itemRepository.deleteAll();
    }

    //@Test
    /*
    public void 재고감소(){
        itemService.decrease("12341234",1);
        //100-1==99
        Item item = itemRepository.findByIsbn("12341234").orElseThrow();

        assertEquals(99,item.getStockQuantity());

    }*/

    @Test
    public void 동시에_100개의_요청() throws InterruptedException{
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++){
            executorService.submit(() -> {
               try {
                   lettuceLockStockFacade.decrease("12341234",1);
               } catch (InterruptedException e) {
                   throw new RuntimeException(e);
               } finally {
                   latch.countDown();
               }
            });
        }

        latch.await();

        Item item = itemRepository.findByIsbn("12341234").orElseThrow();

        assertEquals(0,item.getStockQuantity());

    }

}
