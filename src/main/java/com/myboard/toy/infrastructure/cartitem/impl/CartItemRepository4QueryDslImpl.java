package com.myboard.toy.infrastructure.cartitem.impl;

import com.myboard.toy.domain.cartitem.CartItem;
import com.myboard.toy.domain.cartitem.QCartItem;
import com.myboard.toy.infrastructure.cartitem.CartItemRepository4QueryDsl;
import com.myboard.toy.securityproject.domain.entity.Account;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.Optional;

public class CartItemRepository4QueryDslImpl implements CartItemRepository4QueryDsl {

    private final JPAQueryFactory queryFactory;

    public CartItemRepository4QueryDslImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    QCartItem cartItem = QCartItem.cartItem;

    @Override
    public Optional<CartItem> findByCartAccountAndItemId(Account account, String isbn) {
        CartItem result = queryFactory
                .selectFrom(cartItem)
                .where(
                        cartItem.cart.account.eq(account)
                                .and(cartItem.item.isbn.eq(isbn))
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }
}
