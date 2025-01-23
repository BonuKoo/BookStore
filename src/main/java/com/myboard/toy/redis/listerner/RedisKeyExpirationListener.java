package com.myboard.toy.redis.listerner;

import com.myboard.toy.redis.repository.RedisCommon;
import com.myboard.toy.sales.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisKeyExpirationListener implements MessageListener {

    private final CartService databaseCartService;
    private final RedisCommon redisCommon;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("TTL expired for cart key: {}", expiredKey);

        // cart: prefix 확인
        if (expiredKey.startsWith("cart:")) {
            String userId = expiredKey.substring("cart:".length());

            try {

                // Redis에서 데이터베이스로 동기화 (Redis 데이터를 바로 읽지 않음)
                log.info("Syncing cart for userId: {}", userId);

                databaseCartService.syncCartOnExpiration(userId);

            } catch (Exception e) {
                log.error("Error syncing cart for userId: {}", userId, e);
            }

        }
    }
}
