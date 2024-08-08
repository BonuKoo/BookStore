package com.myboard.toy.domain.cartitem.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CartItemRemoveRequestForm {
    private Long cartId;
    private String itemIsbn;
}
