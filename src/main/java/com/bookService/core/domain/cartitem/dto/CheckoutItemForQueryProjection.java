package com.bookService.core.domain.cartitem.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class CheckoutItemForQueryProjection {

    // CartItem 필드
    private final Long cartItemId;
    private final int amount; // CartItem.amount

    // Item 필드
    private final String isbn;        // Item.isbn (productId)
    private final String title;       // Item.title (orderName)
    private final int price;         // Item.price
    private final Long sellerId;      // Item.sellerId

    @QueryProjection
    public CheckoutItemForQueryProjection(Long cartItemId, int amount, String isbn, String title, int price, Long sellerId) {
        this.cartItemId = cartItemId;
        this.amount = amount;
        this.isbn = isbn;
        this.title = title;
        this.price = price;
        this.sellerId = sellerId;
    }
}
