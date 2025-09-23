package com.bookService.core.domain.cartitem.repository;

import com.bookService.core.domain.cartitem.CartItem;

import java.util.List;

public interface CartItemRepository4QueryDsl {
    List<CartItem> findAllWithItemByIdIn(List<Long> ids);
}
