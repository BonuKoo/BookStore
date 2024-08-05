package com.myboard.toy.application.cart;

import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.domain.cart.dto.CartListDto;
import com.myboard.toy.infrastructure.cart.CartRepository;
import com.myboard.toy.infrastructure.cartitem.CartItemRepository;
import com.myboard.toy.securityproject.domain.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    /* Find */
    public Cart findCartByAccount(Account account){

        Optional<Cart> cart = cartRepository.findByAccount(account);

        if (cart.isEmpty()){
            Cart newCart = new Cart();
            newCart.createCart(account);
            return newCart;
        } else
            return cart.get();

    }

    public Cart getOrCreateCart(Account account){

        Optional<Cart> cartExist = cartRepository.findByAccount(account);

        if (cartExist.isPresent()){
            return cartExist.get();
        }else {

            Cart newCart = new Cart();

            newCart.createCart(account);

            return cartRepository.save(newCart);
        }
    }

    public void save(Cart cart){
        cartRepository.save(cart);
    }

    public List<CartListDto> getCartList(Account account){
        Long cartId = account.getCart().getId();
        return cartRepository.getCartList(cartId);
    };

}
