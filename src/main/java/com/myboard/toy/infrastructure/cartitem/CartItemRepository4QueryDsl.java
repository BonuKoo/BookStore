package com.myboard.toy.infrastructure.cartitem;

import com.myboard.toy.domain.cartitem.CartItem;
import com.myboard.toy.securityproject.domain.entity.Account;

import java.util.Optional;

public interface CartItemRepository4QueryDsl {

    Optional<CartItem> findByCartAccountAndItemId(Account account, String isbn);

}
