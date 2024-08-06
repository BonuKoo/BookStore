package com.myboard.toy.domain.cartitem.dto;

import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.domain.item.Item;
import com.myboard.toy.securityproject.domain.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemUpdateAmountRequestForm {

    private String itemIsbn;    //아이템 Key 값
    private int amount;         //수정할 개수

}
