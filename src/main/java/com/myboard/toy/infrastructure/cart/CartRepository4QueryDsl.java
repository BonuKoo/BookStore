package com.myboard.toy.infrastructure.cart;

import com.myboard.toy.domain.cart.dto.CartListDto;

import java.util.List;

public interface CartRepository4QueryDsl {

    List<CartListDto> getCartList(Long cartId);

}
