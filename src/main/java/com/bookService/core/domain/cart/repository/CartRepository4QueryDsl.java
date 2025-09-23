package com.bookService.core.domain.cart.repository;

import com.bookService.core.domain.cartitem.dto.CartListDTOForQueryProjection;

import java.util.List;

public interface CartRepository4QueryDsl {

    List<CartListDTOForQueryProjection> getCartList(Long cartId);

}
