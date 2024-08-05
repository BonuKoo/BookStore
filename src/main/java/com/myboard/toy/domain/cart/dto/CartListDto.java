package com.myboard.toy.domain.cart.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class CartListDto {

    private String name;
    private Integer price;
    private Integer amount;     //상품마다 몇 개가 담겨있는 지 확인
    private Integer totPrice;   //장바구니에 담긴 상품 하나의 총합

    @QueryProjection
    public CartListDto(String name, Integer price, Integer amount, Integer totPrice) {
        this.name = name;
        this.price = price;
        this.amount = amount;
        this.totPrice = totPrice;
    }

}
