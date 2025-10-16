package com.bookService.core.domain.cartitem.repository;

import com.bookService.core.domain.cartitem.CartItem;
import com.bookService.core.domain.cartitem.dto.CheckoutItemForQueryProjection;
import com.bookService.core.domain.cartitem.dto.CheckoutItemForQueryProjection2;
import com.bookService.core.domain.item.Item;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository4QueryDsl {

    List<CartItem> findAllWithItemByIdInBeforeFixed(List<Long> ids);

    List<CartItem> findAllWithItemByIdIn(List<Long> ids);

    List<CheckoutItemForQueryProjection> customCartItemProjection(List<Long> ids);

    List<CheckoutItemForQueryProjection2> customCartItemProjection2(List<Long> ids);

}
