package com.bookService.core.domain.cartitem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CartItemUpdateRequestForm {

    private String userId;
    private String isbn;
    private int amount;

}
