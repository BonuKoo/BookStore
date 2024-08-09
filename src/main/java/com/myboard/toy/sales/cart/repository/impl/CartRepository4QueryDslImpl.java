package com.myboard.toy.sales.cart.repository.impl;

import com.myboard.toy.domain.cart.QCart;
import com.myboard.toy.sales.domain.dto.CartListDto;
import com.myboard.toy.sales.domain.dto.CartTotalPriceDto;
import com.myboard.toy.domain.cartitem.QCartItem;
import com.myboard.toy.domain.item.QItem;
import com.myboard.toy.sales.cart.repository.CartRepository4QueryDsl;
import com.myboard.toy.security.domain.entity.QAccount;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

public class CartRepository4QueryDslImpl implements CartRepository4QueryDsl {

    private final JPAQueryFactory queryFactory;

    public CartRepository4QueryDslImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    QAccount account = new QAccount(QAccount.account);
    QCart cart = new QCart(QCart.cart);
    QCartItem cartItem = new QCartItem(QCartItem.cartItem);
    QItem item = new QItem(QItem.item);

    //cart id를 받아야 한다.

    @Override
    public List<CartListDto> getCartList(Long cartId) {

        return queryFactory
                .select(Projections.constructor(CartListDto.class,
                        item.title.as("name"),
                        item.price.as("price"),
                        cartItem.count.as("amount"),
                        cartItem.count.multiply(item.price).as("totPrice"),
                        item.isbn.as("isbn")
                ))
                .from(cartItem)
                .join(cartItem.item, item)
                .where(cartItem.cart.id.eq(cartId))
                .fetch();
    }

    @Override
    public CartTotalPriceDto getCartTotalPrice(Long cardId){

        return queryFactory
                .select(Projections.constructor(CartTotalPriceDto.class,
                        cart.totPrice.as("cartTotPrice")
                ))
                .from(cart)
                .where(cart.id.eq(cardId))
                .fetchOne();
    }

}
