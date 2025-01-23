package com.myboard.toy.scheduler;

import com.myboard.toy.redis.repository.RedisCommon;
import com.myboard.toy.sales.cart.service.CartService;
import com.myboard.toy.sales.cartitem.dto.RedisCartDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartSyncScheduler {

    private final RedisCommon redisCommon;
    private final CartService databaseCartService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedRate = 540000) // 매 9분마다 실행
    public void syncCartsToDatabase() {

        log.info("Starting scheduled cart sync to database...");

        Set<String> keys = redisTemplate.keys("cart:*");

        if (keys == null || keys.isEmpty()) {
            log.info("No carts found in Redis to sync.");
            return;
        }

        for (String key : keys) {
            try {

                RedisCartDto cart = redisCommon.getFromHash(key, "cart", RedisCartDto.class);

                if (cart != null) {

                    String userId = key.substring("cart:".length());

                    databaseCartService.saveCartToDatabase(userId, cart);

                //    redisTemplate.delete(key);

                    log.info("Cart for userId: {} synced and removed from Redis.", userId);

                }
            } catch (Exception e) {
                log.error("Error syncing cart for key: {}", key, e);
            }
        }
    }

}