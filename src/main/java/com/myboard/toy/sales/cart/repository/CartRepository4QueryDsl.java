package com.myboard.toy.sales.cart.repository;

import com.myboard.toy.sales.domain.dto.CartListDto;
import com.myboard.toy.sales.domain.dto.CartTotalPriceDto;

import java.util.List;

public interface CartRepository4QueryDsl {

    List<CartListDto> getCartList(Long cartId);
    public CartTotalPriceDto getCartTotalPrice(Long cardId);

}
