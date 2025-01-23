package com.myboard.toy.sales.cart.service;

import com.myboard.toy.redis.repository.RedisCommon;
import com.myboard.toy.sales.cart.strategy.CartDataStrategy;
import com.myboard.toy.sales.cartitem.dto.RedisCartDto;
import com.myboard.toy.sales.cartitem.dto.RedisCartItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisCartService {

    private static final String CART_KEY_PREFIX = "cart:"; // Redis Key Prefix
    private static final long REDIS_EXPIRATION_TIME = 10;


    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisCommon redisCommon;
    private final CartDataStrategy cartDataStrategy;


    /**
     * 유저 ID로 Redis에 저장된 장바구니 조회
     */
    // Redis에서 장바구니를 가져오거나 없으면 DB에서 가져온 후 Redis에 저장
    public RedisCartDto getOrCreateCart(String userId) {
        String key = CART_KEY_PREFIX + userId;

        // Redis에서 데이터 조회
        RedisCartDto cart = redisCommon.getFromHash(key, "cart", RedisCartDto.class);

        if (cart == null) {
            log.info("Cart not found in Redis. Fetching from DB for userId: {}", userId);
            cart = cartDataStrategy.fetchCartFromDatabase(userId);

            // Redis에 저장
            redisCommon.putInHash(key, "cart", cart);

            redisTemplate.expire(key, REDIS_EXPIRATION_TIME, TimeUnit.MINUTES);

        }

        return cart;
    }

    /**
        TTL 만료 전에 데이터를 DB에 동기화
     */


    /**
        아이템 수량 변경
    **/

    public void updateItemQuantity(String userId, String itemId, int newCount) {
        String key = CART_KEY_PREFIX + userId;

        // Redis에서 장바구니 가져오기
        RedisCartDto cart = getOrCreateCart(userId);

        // 아이템이 존재하는지 확인
        if (!cart.getItems().containsKey(itemId)) {
            throw new IllegalArgumentException("Item not found in cart.");
        }
        RedisCartItemDto item = cart.getItems().get(itemId);

        if (newCount <= 0) {
            // 수량이 0 이하이면 아이템 제거
            cart.removeItem(itemId);

        } else {

            // 수량 업데이트
            int oldCount = item.getCount();

            int priceDifference = (newCount - oldCount) * item.getItemPrice();

            item.setCount(newCount);

            cart.updateTotalPrice(priceDifference);
        }

        // 마지막 업데이트 시간 갱신
        cart.updateLastUpdated();

        // Redis에 업데이트된 장바구니 저장
        redisCommon.putInHash(key, "cart", cart);
        redisTemplate.expire(key, REDIS_EXPIRATION_TIME, TimeUnit.MINUTES);

    }

    /**
        아이템 추가
     */
    public void addItemToCart(String userId, RedisCartItemDto newItem) {
        String key = CART_KEY_PREFIX + userId;

        // Redis에서 장바구니 가져오기
        RedisCartDto cart = getOrCreateCart(userId);

        // 아이템 추가
        cart.addItem(newItem);

        // Redis에 업데이트된 장바구니 저장
        redisCommon.putInHash(key, "cart", cart);
        redisTemplate.expire(key, REDIS_EXPIRATION_TIME, TimeUnit.MINUTES);
    }
    /**
        아이템 삭제
     */
    public void removeItemFromCart(String userId, String itemId) {
        String key = CART_KEY_PREFIX + userId;

        // Redis에서 장바구니 가져오기
        RedisCartDto cart = getOrCreateCart(userId);

        // 아이템 제거
        cart.removeItem(itemId);

        // Redis에 업데이트된 장바구니 저장
        redisCommon.putInHash(key, "cart", cart);
        redisTemplate.expire(key, REDIS_EXPIRATION_TIME, TimeUnit.MINUTES);
    }
    /**
     *  장바구니 비우기
    **/
    public void clearCart(String userId) {
        String key = CART_KEY_PREFIX + userId;

        // Redis에서 장바구니 가져오기
        RedisCartDto cart = getOrCreateCart(userId);

        // 장바구니 초기화
        cart.clearCart();

        // Redis에 업데이트된 장바구니 저장
        redisCommon.putInHash(key, "cart", cart);
        redisTemplate.expire(key, REDIS_EXPIRATION_TIME, TimeUnit.MINUTES);
    }

}
