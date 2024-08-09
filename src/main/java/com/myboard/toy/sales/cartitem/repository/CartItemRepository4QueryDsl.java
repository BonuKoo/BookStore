package com.myboard.toy.sales.cartitem.repository;

import com.myboard.toy.sales.domain.CartItem;
import com.myboard.toy.security.domain.entity.Account;

import java.util.Optional;

public interface CartItemRepository4QueryDsl {

    Optional<CartItem> findByCartAccountAndItemId(Account account, String isbn);

}
