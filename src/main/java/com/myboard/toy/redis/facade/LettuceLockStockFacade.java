package com.myboard.toy.redis.facade;

import com.myboard.toy.redis.repository.RedisLockRepository;
import com.myboard.toy.sales.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LettuceLockStockFacade {

    private final RedisLockRepository redisLockRepository;
    private final ItemService itemService;

    public void decrease(String isbn, int quantity) throws InterruptedException{

        while (!redisLockRepository.lock(isbn)){
            Thread.sleep(100);
        }try {
            itemService.decrease(isbn,quantity);
        }finally {
            redisLockRepository.unlock(isbn);
        }
    }

}
