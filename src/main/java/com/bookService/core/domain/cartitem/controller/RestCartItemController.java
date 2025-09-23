package com.bookService.core.domain.cartitem.controller;

import com.bookService.core.domain.cart.Cart;
import com.bookService.core.domain.cart.service.CartService;

import com.bookService.core.domain.cartitem.dto.CartItemUpdateAmountRequestForm;
import com.bookService.core.domain.cartitem.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/cartItem")
@RequiredArgsConstructor
public class RestCartItemController {

    private final CartItemService cartItemService;
    private final CartService cartService;

    /* 증가 */
    @PostMapping("/increaseItem")
    public ResponseEntity<Map<String, String>> increaseAmount(
            @AuthenticationPrincipal String userId,
            @RequestBody CartItemUpdateAmountRequestForm form
    ) {
        Map<String, String> response = new HashMap<>();
        try {
            Long accountId = Long.parseLong(userId);
            Cart cart = cartService.findCartByAccountIdLongType(accountId);

            cartItemService.updateCartItemAmount(cart, form); // 수량 증가 처리

            response.put("message", "Item quantity increased successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Failed to update item quantity.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /* 감소 */
    @PostMapping("/decreaseItem")
    public ResponseEntity<Map<String, String>> decreaseAmount(
            @AuthenticationPrincipal String userId,
            @RequestBody CartItemUpdateAmountRequestForm form
    ) {
        Map<String, String> response = new HashMap<>();
        try {
            Long accountId = Long.parseLong(userId);
            Cart cart = cartService.findCartByAccountIdLongType(accountId);

            form.setAmount(Math.max(0, form.getAmount()));
            cartItemService.updateCartItemAmount(cart, form);

            response.put("message", "Item quantity decreased successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to update item quantity.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /* DELETE */
    @PostMapping("/removeItem")
    public ResponseEntity<Map<String, String>> deleteCartItem(
            @AuthenticationPrincipal String userId,
            @RequestParam String itemIsbn
    ) {
        Map<String, String> response = new HashMap<>();

        try {
            cartItemService.removeCartItem(userId, itemIsbn);
            response.put("message", "Item removed successfully.");
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to remove item.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
