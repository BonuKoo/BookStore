package com.myboard.toy.domain.cartitem.dto;

import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.domain.item.Item;
import com.myboard.toy.securityproject.domain.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CartItemUpdateRequestForm {

    private Account account;
    private Item item;
    private Cart cart;
    private int amount;
    
}
