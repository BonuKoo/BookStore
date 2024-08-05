package com.myboard.toy.infrastructure.cart;

import com.myboard.toy.domain.cart.dto.CartListDto;
import com.myboard.toy.domain.cartitem.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartRepository4QueryDsl {

    List<CartListDto> getCartList(Long cartId);

}
