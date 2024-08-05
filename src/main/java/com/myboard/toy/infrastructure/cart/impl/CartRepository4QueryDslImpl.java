package com.myboard.toy.infrastructure.cart.impl;

import com.myboard.toy.domain.cart.QCart;
import com.myboard.toy.domain.cart.dto.CartListDto;
import com.myboard.toy.domain.cartitem.CartItem;
import com.myboard.toy.domain.cartitem.QCartItem;
import com.myboard.toy.domain.item.QItem;
import com.myboard.toy.infrastructure.cart.CartRepository4QueryDsl;
import com.myboard.toy.securityproject.domain.entity.QAccount;
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
        List<CartListDto> cartListDtos = queryFactory
                .select(Projections.constructor(CartListDto.class,
                        item.title.as("name"),
                        item.price.as("price"),
                        cartItem.count.as("amount"),
                        cartItem.count.multiply(item.price).as("totPrice"),
                        cart.totPrice.as("cartTotPrice")
                ))
                .from(cartItem)
                .join(cartItem.item, item)
                .where(cartItem.cart.id.eq(cartId))
                .fetch();

        return cartListDtos;
    }




}
/*

    private String name;
    private String price;
    private String amount;  //상품마다 몇 개가 담겨있는 지 확인
    private int eachPrice;  //상품 개별 가격
    private int totPrice;   //장바구니에 담긴 총 가격


 */