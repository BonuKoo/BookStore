package com.myboard.toy.application.cartitem;

import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.domain.cartitem.CartItem;
import com.myboard.toy.domain.item.Item;
import com.myboard.toy.infrastructure.cartitem.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;

    public CartItem createCartItem(Cart cart, Item item, int amount){
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .item(item)
                .count(amount)
                .build();
        return cartItem;
    }

    public void saveCartItem(CartItem cartItem) {
        cartItemRepository.save(cartItem);
    }

}
