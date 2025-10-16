package com.bookService.core.domain.cartitem.repository.impl;

import com.bookService.core.domain.cartitem.CartItem;
import com.bookService.core.domain.cartitem.QCartItem;
import com.bookService.core.domain.cartitem.dto.CheckoutItemForQueryProjection;
import com.bookService.core.domain.cartitem.dto.CheckoutItemForQueryProjection2;
import com.bookService.core.domain.cartitem.repository.CartItemRepository4QueryDsl;
import com.bookService.core.domain.item.QItem;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

public class CartItemRepository4QueryDslImpl implements CartItemRepository4QueryDsl {

    private final JPAQueryFactory queryFactory;

    public CartItemRepository4QueryDslImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    QCartItem cartItem = QCartItem.cartItem;
    QItem item = QItem.item;

    // 강제 N+1 문제 유발 - Item
    @Override
    public List<CartItem> findAllWithItemByIdInBeforeFixed(List<Long> ids) {
        return queryFactory
                .selectFrom(cartItem)
                .where(cartItem.id.in(ids))
                .fetch();
    }

    // Fetch Join - Item
    @Override
    public List<CartItem> findAllWithItemByIdIn(List<Long> ids) {
        return queryFactory
                .selectFrom(cartItem)
                .join(cartItem.item,item).fetchJoin()
                .where(cartItem.id.in(ids))
                .fetch();
    }

    @Override
    public List<CheckoutItemForQueryProjection> customCartItemProjection(List<Long> ids) {
        return queryFactory
                .select(Projections.constructor(CheckoutItemForQueryProjection.class,
                        cartItem.id,
                        cartItem.amount,
                        item.isbn,
                        item.title,
                        item.price,
                        item.sellerId
                        ))
                .from(cartItem)
                .join(cartItem.item, item)
                .where(cartItem.id.in(ids))
                .fetch();
    }

    @Override
    public List<CheckoutItemForQueryProjection2> customCartItemProjection2(List<Long> ids) {
        return queryFactory
                .select(Projections.constructor(CheckoutItemForQueryProjection2.class,
                        cartItem.id,
                        cartItem.amount,
                        cartItem.cart.account,
                        item.isbn,
                        item.title,
                        item.price,
                        item.sellerId
                ))
                .from(cartItem)
                .join(cartItem.item, item)
                .where(cartItem.id.in(ids))
                .fetch();
    }

}
