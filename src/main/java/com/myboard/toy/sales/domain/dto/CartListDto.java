package com.myboard.toy.sales.domain.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class CartListDto {

    private String name;
    private Integer price;
    private Integer amount;     //상품마다 몇 개가 담겨있는 지 확인
    private Integer totPrice;   //장바구니에 담긴 상품 하나의 총합
    private String isbn;

    @QueryProjection
    public CartListDto(String name, Integer price, Integer amount, Integer totPrice,String isbn) {
        this.name = name;
        this.price = price;
        this.amount = amount;
        this.totPrice = totPrice;
        this.isbn = isbn;
    }

}
