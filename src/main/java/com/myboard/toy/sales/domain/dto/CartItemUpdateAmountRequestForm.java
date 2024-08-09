package com.myboard.toy.sales.domain.dto;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemUpdateAmountRequestForm {

    private String itemIsbn;    //아이템 Key 값
    private int amount;         //수정할 개수

}
