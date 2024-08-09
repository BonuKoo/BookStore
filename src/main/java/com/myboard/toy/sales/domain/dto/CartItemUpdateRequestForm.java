package com.myboard.toy.sales.domain.dto;

import com.myboard.toy.sales.domain.Cart;
import com.myboard.toy.sales.domain.Item;
import com.myboard.toy.security.domain.entity.Account;
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
