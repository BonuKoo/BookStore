package com.myboard.toy.domain.cart.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class CartTotalPriceDto {

    private Integer cartTotPrice;

    @QueryProjection
    public CartTotalPriceDto(Integer cartTotPrice) {
        this.cartTotPrice = cartTotPrice;
    }
}
