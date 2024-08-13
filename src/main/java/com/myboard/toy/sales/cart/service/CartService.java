package com.myboard.toy.sales.cart.service;

import com.myboard.toy.sales.domain.Cart;
import com.myboard.toy.sales.domain.dto.CartDto;
import com.myboard.toy.sales.domain.dto.CartListDto;
import com.myboard.toy.sales.domain.dto.CartTotalPriceDto;
import com.myboard.toy.sales.cart.repository.CartRepository;
import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    /* Find */
    public Cart findCartByAccountId(AccountDto dto){

        Long accountId = dto.getId();
        Optional<Account> accountOpt = userRepository.findById(accountId);
        Account account = accountOpt.orElseThrow();

        Optional<Cart> cart = cartRepository.findByAccount(account);

        if (cart.isEmpty()){
            Cart newCart = new Cart();
            newCart.createCart(account);
            return newCart;
        } else
            return cart.get();
    }

    public CartDto getCartIdByAccountId(AccountDto dto){
        Long accountId = dto.getId();
        Optional<Account> accountOpt = userRepository.findById(accountId);
        Account account = accountOpt.orElseThrow();
        Long cardId = account.getCart().getId();
        return CartDto.builder()
                .id(cardId)
                .build();
    }

    public Cart getOrCreateCart(AccountDto accountDto){
        Long accountId = accountDto.getId();
        Optional<Account> accountOpt = userRepository.findById(accountId);
        Account account = accountOpt.orElseThrow();
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


    public List<CartListDto> getCartList(AccountDto dto){

        Long accountId = dto.getId();
        Optional<Account> accountOpt = userRepository.findById(accountId);
        Account account = accountOpt.orElseThrow();

        Long cartId = account.getCart().getId();
        return cartRepository.getCartList(cartId);
    };

    public CartTotalPriceDto getCartTotalPrice(AccountDto dto){

        Long accountId = dto.getId();
        Optional<Account> accountOpt = userRepository.findById(accountId);
        Account account = accountOpt.orElseThrow();


        Long cartId = account.getCart().getId();

        List<CartListDto> cartList = cartRepository.getCartList(cartId);

        int extractdTotalPrice = cartList.stream()
                .mapToInt(CartListDto::getTotPrice)
                .sum();
        CartTotalPriceDto totalPrice = new CartTotalPriceDto(extractdTotalPrice);
        return totalPrice;
    };

}
