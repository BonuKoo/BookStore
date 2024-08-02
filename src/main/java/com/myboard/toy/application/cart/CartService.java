package com.myboard.toy.application.cart;

import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.infrastructure.cart.CartRepository;
import com.myboard.toy.infrastructure.cartitem.CartItemRepository;
import com.myboard.toy.securityproject.domain.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public Cart findCartByAccount(Account account){

        Optional<Cart> cart = cartRepository.findByAccount(account);

        if (cart.isEmpty()){
            Cart newCart = new Cart();
            newCart.createCart(account);
            return newCart;
        } else
            return cart.get();

    }

    public void save(Cart cart){
        cartRepository.save(cart);
    }
}

/*
*
* findCartByAccount
* 값을 받아서,
* */