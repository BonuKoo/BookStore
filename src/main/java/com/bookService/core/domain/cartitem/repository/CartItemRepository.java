package com.bookService.core.domain.cartitem.repository;

import com.bookService.core.domain.cart.Cart;
import com.bookService.core.domain.cartitem.CartItem;
import com.bookService.core.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem,Long>,CartItemRepository4QueryDsl {

    CartItem findByCartId(Long id);

    Optional<CartItem> findByCartAndItem(Cart cart, Item item);

    Optional<CartItem> findByCartIdAndItemIsbn(Long cardId, String itemIsbn);

}
