package com.myboard.toy.sales.cartitem.service;

import com.myboard.toy.sales.cart.service.CartService;
import com.myboard.toy.sales.item.service.ItemService;
import com.myboard.toy.sales.domain.Cart;
import com.myboard.toy.sales.domain.CartItem;
import com.myboard.toy.sales.domain.dto.CartItemRemoveRequestForm;
import com.myboard.toy.sales.domain.dto.CartItemUpdateAmountRequestForm;
import com.myboard.toy.sales.domain.dto.CartItemUpdateRequestForm;
import com.myboard.toy.sales.domain.Item;
import com.myboard.toy.sales.cart.repository.CartRepository;
import com.myboard.toy.sales.cartitem.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ItemService itemService;
    private final CartService cartService;

    public CartItem createCartItemOrIncreaseAmount(CartItemUpdateRequestForm form){

        Optional<CartItem> existingCartItemOpt = cartItemRepository.findByCartAndItem(form.getCart(), form.getItem());

        if (existingCartItemOpt.isPresent()){
            CartItem cartItem = existingCartItemOpt.get();
            cartItem.addCount(form.getAmount());
            form.getCart().updateTotPrice(form.getAmount() * form.getItem().getPrice());

            return cartItemRepository.save(cartItem);

        }else {

            CartItem cartItem = CartItem.builder()
                    .cart(form.getCart())
                    .item(form.getItem())
                    .count(form.getAmount())
                    .build();

            Cart cart = form.getCart();
            int amount = form.getAmount();
            Item item = form.getItem();

            cart.updateTotPrice(amount * item.getPrice());

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
    public void updateCartItemAmount(Cart cart,CartItemUpdateAmountRequestForm form) {

        Item item = itemService.findByIsbn(form.getItemIsbn());

        CartItem cartItem = cartItemRepository.findByCartAndItem(cart, item)
                .orElseThrow(() -> new IllegalArgumentException("해당 CartItem이 존재하지 않습니다."));

        cartItem.updateCount(form.getAmount());

        cartItemRepository.save(cartItem);
    }

    /* Delete*/

    @Transactional
    public void removeCartItem(CartItemRemoveRequestForm form){
        form.getCartId();
        form.getItemIsbn();

        Optional<CartItem> cartItem = cartItemRepository.findByCartIdAndItemIsbn(form.getCartId(), form.getItemIsbn());

        if (cartItem.isPresent()){
            cartItemRepository.delete(cartItem.get());
        }else {
            throw new NoSuchElementException("Item with ISBN " + form.getItemIsbn() + " not found in cart with ID " + form.getCartId());
        }
    }
}

