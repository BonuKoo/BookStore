package com.myboard.toy.sales.cart.strategy;

import com.myboard.toy.sales.cart.repository.CartRepository;
import com.myboard.toy.sales.cartitem.dto.RedisCartDto;
import com.myboard.toy.sales.cartitem.dto.RedisCartItemDto;
import com.myboard.toy.sales.domain.entity.Cart;
import org.springframework.stereotype.Component;

@Component
public class DefaultCartDataStrategy implements CartDataStrategy{

    private final CartRepository cartRepository;

    public DefaultCartDataStrategy(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public RedisCartDto fetchCartFromDatabase(String userId) {
        Cart dbCart = cartRepository.findByAccount_Id(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("Cart not found for userId: " + userId));
        return mapDbCartToRedisCart(dbCart);
    }

    // DB 데이터를 Redis DTO로 변환
    private RedisCartDto mapDbCartToRedisCart(Cart dbCart) {
        RedisCartDto redisCart = new RedisCartDto();

        redisCart.setTotalPrice(dbCart.getTotPrice());
        dbCart.getCartItems().forEach(cartItem -> {
            RedisCartItemDto redisCartItem = new RedisCartItemDto(
                    String.valueOf(cartItem.getItem().getIsbn()),
                    cartItem.getItem().getTitle(),
                    cartItem.getCount(),
                    cartItem.getItem().getPrice()
                    );
            redisCart.addItem(redisCartItem);
        });

        redisCart.setStatus("ACTIVE");
        redisCart.updateLastUpdated();
        return redisCart;
    }

}
