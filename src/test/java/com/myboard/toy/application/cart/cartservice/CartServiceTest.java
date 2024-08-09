package com.myboard.toy.application.cart.cartservice;

import com.myboard.toy.sales.cart.service.CartService;
import com.myboard.toy.sales.domain.Cart;
import com.myboard.toy.sales.domain.CartItem;
import com.myboard.toy.sales.domain.Item;
import com.myboard.toy.sales.cart.repository.CartRepository;
import com.myboard.toy.sales.item.repository.ItemRepository;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class CartServiceTest {

    @Autowired
    private CartService cartService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CartRepository cartRepository;

    @Test
    @Transactional
    void saveCartTest(){

        //Given

        //로그인 데이터
        Optional<Account> optionalAccount = userRepository.findById(1L);
        Account account = optionalAccount.orElseThrow();

        //고를 상품
        Optional<Item> optionalItem = itemRepository.findByIsbn("9781803064130");
        Item item = optionalItem.orElseThrow();

        int amount = 1;

        //장바구니 생성
        Cart cart = new Cart();
        cart.createCart(account);

        //장바구니에 새 상품을 추가할 때 사용될 CartItem
        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .item(item)
                .count(amount)
                .build();
        //장바구니에 CartItem 추가
        cart.getCartItems().add(cartItem);

        //CartItem 및 Cart를 저장
        cartRepository.save(cart);

        //When
        Cart savedCart = cartRepository.findById(cart.getId()).orElseThrow();

        //Then
        assertThat(savedCart).isNotNull();
        assertThat(savedCart.getCartItems()).hasSize(1);

        CartItem savedCartItem = savedCart.getCartItems().get(0);
        assertThat(savedCartItem.getItem()).isEqualTo(item);
        assertThat(savedCartItem.getCount()).isEqualTo(1); // 추가된 수량

    }

}

