package com.bookService.core.domain.cartitem.service;

import com.bookService.core.domain.cart.Cart;
import com.bookService.core.domain.cart.service.CartService;
import com.bookService.core.domain.cartitem.CartItem;
import com.bookService.core.domain.cartitem.dto.CartItemUpdateAmountRequestForm;
import com.bookService.core.domain.cartitem.dto.CartItemUpdateRequestForm;
import com.bookService.core.domain.cartitem.repository.CartItemRepository;
import com.bookService.core.domain.item.Item;
import com.bookService.core.domain.item.service.ItemService;
import com.bookService.core.domain.login.dto.AccountDTO;
import com.bookService.core.domain.login.repository.AccountJpaRepository;
import com.bookService.core.domain.login.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ItemService itemService;
    private final CartService cartService;
    private final AccountService userService;

    public CartItem createCartItemOrIncreaseAmount(CartItemUpdateRequestForm form){

        // 1. JWT에서 받은 userId로 Account 조회
        AccountDTO accountDTO = userService.getAccountById(Long.parseLong(form.getUserId()));

        Long accountDTOId = accountDTO.getId();
        // 2. Cart 가져오기 또는 생성
        Cart cart = cartService.findCartByAccountIdLongType(accountDTOId);

        // 3. Item 조회
        Item item = itemService.findByIsbn(form.getIsbn());

        Optional<CartItem> existingCartItemOpt = cartItemRepository.findByCartAndItem(cart, item);

        if (existingCartItemOpt.isPresent()){
            CartItem cartItem = existingCartItemOpt.get();
            cartItem.addCount(form.getAmount());
            cart.recalcTotPrice();                  // totPrice 다시 계산

            return cartItem;
                    //cartItemRepository.save(cartItem); 변경 감지 되나 안 되나 보자.

        }else {
            CartItem cartItem = new CartItem(cart, item, form.getAmount());
            return cartItemRepository.save(cartItem);
        }
    }

    public void saveCartItem(CartItem cartItem) {

        //cartItem을 먼저 저장.
        cartItemRepository.save(cartItem);

        //cart가 아직 저장되지 않은 경우 저장
        Cart cart = cartItem.getCart();

        if (cart.getId() == null){
            cartService.save(cart);
        }

    }

    @Transactional
    public void updateCartItemAmount(Cart cart, CartItemUpdateAmountRequestForm form) {

        Item item = itemService.findByIsbn(form.getItemIsbn());

        CartItem cartItem = cartItemRepository.findByCartAndItem(cart, item)
                .orElseThrow(() -> new IllegalArgumentException("해당 CartItem이 존재하지 않습니다."));


        cartItem.updateCount(form.getAmount());
        cartItemRepository.save(cartItem);

    }

    @Transactional
    public void updateCartItemAmountAdditive(Cart cart, CartItemUpdateAmountRequestForm form) {
        Item item = itemService.findByIsbn(form.getItemIsbn());

        CartItem cartItem = cartItemRepository.findByCartAndItem(cart, item)
                .orElseThrow(() -> new IllegalArgumentException("해당 CartItem이 존재하지 않습니다."));

        // 기존 수량 + 입력 수량
        int newAmount = cartItem.getAmount() + form.getAmount();
        cartItem.updateCount(newAmount);

        cart.recalcTotPrice();
        cartItemRepository.save(cartItem);
    }

    /* Delete*/

    @Transactional
    public void removeCartItem(String userId, String itemIsbn){

        Long accountId = Long.parseLong(userId);

        Cart cart = cartService.findCartByAccountIdLongType(accountId);

        Optional<CartItem> cartItem = cartItemRepository.findByCartIdAndItemIsbn(cart.getId(), itemIsbn);

        if (cartItem.isPresent()){
            cartItemRepository.delete(cartItem.get());
        }else {
            throw new NoSuchElementException("Item with ISBN " + itemIsbn + " not found in cart with ID " + cart.getId());
        }
    }

    @Transactional
    public void clearCart(String userId) {
        // 1. JWT에서 받은 userId로 Account 조회
        AccountDTO accountDTO = userService.getAccountById(Long.parseLong(userId));

        Long accountDTOId = accountDTO.getId();
        // 2. Cart 가져오기 또는 생성
        Cart cart = cartService.findCartByAccountIdLongType(accountDTOId);

        // cartItemRepository 통해 삭제
        cartItemRepository.deleteAll(cart.getCartItems());

        // 영속성 컨텍스트에서도 관계 제거
        cart.getCartItems().clear();
        cart.recalcTotPrice();

        cartService.save(cart); // cart 총액 갱신
    }

}
