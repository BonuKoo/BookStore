package com.myboard.toy.controller.cartitem;

import com.myboard.toy.application.cart.CartService;
import com.myboard.toy.application.cartitem.CartItemService;
import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.domain.cartitem.dto.CartItemUpdateAmountRequestForm;
import com.myboard.toy.securityproject.domain.entity.Account;
import com.myboard.toy.securityproject.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class RestCartItemController {

    private final CartItemService cartItemService;
    private final CartService cartService;
    private final AccountUtils accountUtils;

    @PostMapping("/increaseItem")
    public ResponseEntity<String> increaseAmountWhenPushTheButton(
            Principal principal,
            @RequestBody CartItemUpdateAmountRequestForm form
    ) {
        try {
            Account account = accountUtils.getAccountByPrincipal((UsernamePasswordAuthenticationToken) principal);
            Cart cart = cartService.findCartByAccount(account);

            form.setAmount(form.getAmount() + 1); // 수량 증가

            cartItemService.updateCartItemAmount(cart, form);

            return ResponseEntity.ok("Item quantity increased successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update item quantity.");
        }
    }

    @PostMapping("/decreaseItem")
    public ResponseEntity<String> decreaseAmountWhenPushTheButton(
            Principal principal,
            @RequestBody CartItemUpdateAmountRequestForm form
    ) {
        try {
            Account account = accountUtils.getAccountByPrincipal((UsernamePasswordAuthenticationToken) principal);
            Cart cart = cartService.findCartByAccount(account);

            form.setAmount(Math.max(0, form.getAmount() - 1)); // 수량 감소

            cartItemService.updateCartItemAmount(cart, form);

            return ResponseEntity.ok("Item quantity decreased successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update item quantity.");
        }
    }
}
