package com.myboard.toy.sales.cart.service;

import com.myboard.toy.sales.domain.Cart;
import com.myboard.toy.sales.domain.dto.CartListDto;
import com.myboard.toy.sales.domain.dto.CartTotalPriceDto;
import com.myboard.toy.sales.cart.repository.CartRepository;
import com.myboard.toy.sales.cartitem.repository.CartItemRepository;
import com.myboard.toy.security.domain.entity.Account;
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

    public CartTotalPriceDto getCartTotalPrice(Account account){
        Long cartId = account.getCart().getId();

        List<CartListDto> cartList = cartRepository.getCartList(cartId);

        int extractdTotalPrice = cartList.stream()
                .mapToInt(CartListDto::getTotPrice)
                .sum();
        CartTotalPriceDto totalPrice = new CartTotalPriceDto(extractdTotalPrice);
        return totalPrice;
    };

}
