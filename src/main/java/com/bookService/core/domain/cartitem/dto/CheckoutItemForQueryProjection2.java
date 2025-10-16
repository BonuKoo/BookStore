package com.bookService.core.domain.cartitem.dto;

import com.bookService.core.domain.login.entity.AccountEntity;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class CheckoutItemForQueryProjection2 {

    // CartItem 필드
    private final Long cartItemId;
    private final int amount; // CartItem.amount
    private AccountEntity accountEntity;

    // Item 필드
    private final String isbn;        // Item.isbn (productId)
    private final String title;       // Item.title (orderName)
    private final int price;         // Item.price
    private final Long sellerId;      // Item.sellerId

    @QueryProjection
    public CheckoutItemForQueryProjection2(Long cartItemId, int amount,AccountEntity accountEntity,String isbn, String title, int price, Long sellerId) {
        this.cartItemId = cartItemId;
        this.amount = amount;
        this.accountEntity = accountEntity;
        this.isbn = isbn;
        this.title = title;
        this.price = price;
        this.sellerId = sellerId;
    }
}
