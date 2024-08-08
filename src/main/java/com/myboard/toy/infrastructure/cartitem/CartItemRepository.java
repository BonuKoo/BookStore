package com.myboard.toy.infrastructure.cartitem;

import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.domain.cartitem.CartItem;
import com.myboard.toy.domain.item.Item;
import com.myboard.toy.securityproject.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem,Integer>, CartItemRepository4QueryDsl {

    CartItem findByCartId(Long id);

    Optional<CartItem> findByCartAndItem(Cart cart, Item item);

}
