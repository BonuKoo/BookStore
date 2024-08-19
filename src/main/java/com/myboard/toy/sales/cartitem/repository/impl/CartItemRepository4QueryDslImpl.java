package com.myboard.toy.sales.cartitem.repository.impl;

import com.myboard.toy.sales.domain.entity.CartItem;
import com.myboard.toy.sales.cartitem.repository.CartItemRepository4QueryDsl;
import com.myboard.toy.sales.domain.entity.QCartItem;
import com.myboard.toy.security.domain.entity.Account;
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
