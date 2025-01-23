package com.myboard.toy.sales.cart.strategy;

import com.myboard.toy.sales.cartitem.dto.RedisCartDto;

public interface CartDataStrategy {

    RedisCartDto fetchCartFromDatabase(String userId);

}
