package com.myboard.toy.application.cart;

import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.infrastructure.cart.CartRepository;
import com.myboard.toy.infrastructure.cartitem.CartItemRepository;
import com.myboard.toy.securityproject.domain.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public Cart findCartByAccount(Account account){
        return cartRepository.findByAccount(account)
                .orElse(null);
    }

    public void save(Cart cart){
        cartRepository.save(cart);
    }
}
