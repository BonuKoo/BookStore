package com.bookService.core.domain.cart.repository.impl;

import com.bookService.core.domain.cart.Cart;
import com.bookService.core.domain.cart.QCart;
import com.bookService.core.domain.cart.repository.CartRepository4QueryDsl;
import com.bookService.core.domain.cartitem.QCartItem;
import com.bookService.core.domain.cartitem.dto.CartListDTOForQueryProjection;
import com.bookService.core.domain.item.QItem;
import com.bookService.core.domain.login.entity.QAccountEntity;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

public class CartRepository4QueryDslImpl implements CartRepository4QueryDsl {
    private final JPAQueryFactory queryFactory;

    public CartRepository4QueryDslImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    QAccountEntity account = new QAccountEntity(QAccountEntity.accountEntity);
    QCart cart = new QCart(QCart.cart);
    QCartItem cartItem = new QCartItem(QCartItem.cartItem);
    QItem item = new QItem(QItem.item);

    //cart id를 받아야 한다.
    @Override
    public List<CartListDTOForQueryProjection> getCartList(Long cartId) {

        return queryFactory
                .select(Projections.constructor(CartListDTOForQueryProjection.class,
                        item.title.as("name"),
                        item.price.as("price"),
                        cartItem.amount.as("amount"),
                        cartItem.amount.multiply(item.price).as("totPrice"),
                        item.isbn.as("isbn"),
                        cartItem.id
                ))
                .from(cartItem)
                .join(cartItem.item, item)
                .where(cartItem.cart.id.eq(cartId))
                .fetch();
    }
}
