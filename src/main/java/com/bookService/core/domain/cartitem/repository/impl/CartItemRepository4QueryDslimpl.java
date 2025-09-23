package com.bookService.core.domain.cartitem.repository.impl;

import com.bookService.core.domain.cartitem.CartItem;
import com.bookService.core.domain.cartitem.QCartItem;
import com.bookService.core.domain.cartitem.repository.CartItemRepository4QueryDsl;
import com.bookService.core.domain.item.QItem;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

public class CartItemRepository4QueryDslimpl implements CartItemRepository4QueryDsl {

    private final JPAQueryFactory queryFactory;

    QCartItem cartItem = QCartItem.cartItem;
    QItem item = QItem.item;

    public CartItemRepository4QueryDslimpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<CartItem> findAllWithItemByIdIn(List<Long> ids) {
        return queryFactory
                .selectFrom(cartItem)
                .join(cartItem.item,item).fetchJoin()
                .where(cartItem.id.in(ids))
                .fetch();
    }
}
