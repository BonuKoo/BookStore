package com.myboard.toy.sales.cartitem.repository;

import com.myboard.toy.sales.domain.Cart;
import com.myboard.toy.sales.domain.CartItem;
import com.myboard.toy.sales.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem,Integer>, CartItemRepository4QueryDsl {

    CartItem findByCartId(Long id);

    Optional<CartItem> findByCartAndItem(Cart cart, Item item);

    Optional<CartItem> findByCartIdAndItemIsbn(Long cardId, String itemIsbn);


}
