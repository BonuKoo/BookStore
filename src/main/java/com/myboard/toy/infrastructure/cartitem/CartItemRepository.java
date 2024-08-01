package com.myboard.toy.infrastructure.cartitem;

import com.myboard.toy.domain.cartitem.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem,Long> {
}
