package com.bookService.core.domain.cart.controller;
/*
import com.bookService.core.domain.cart.dto.CartDocument;
import com.bookService.core.domain.cart.service.CartMongoService;
import com.bookService.core.domain.cartitem.dto.CartItemDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
*/
/*
@RestController
@RequestMapping("/mongo/cart")
@RequiredArgsConstructor
 */
public class CartMongoController {
/*
    private final CartMongoService cartService;

    @GetMapping("/list")
    public ResponseEntity<CartDocument> getCart(@AuthenticationPrincipal String userId) {
        CartDocument cartItems = cartService.getCart(userId);
        return ResponseEntity.ok(cartItems);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addItem(
            @AuthenticationPrincipal String userId,
            @RequestBody CartItemDocument item
    ) {
        cartService.addItem(userId, item);
        return ResponseEntity.ok("Item added successfully.");
    }

    @PostMapping("/remove")
    public ResponseEntity<String> removeItem(
            @AuthenticationPrincipal String userId,
            @RequestParam String itemIsbn
    ) {
        cartService.removeItem(userId, itemIsbn);
        return ResponseEntity.ok("Item removed successfully.");
    }

    @PostMapping("/increaseItem")
    public ResponseEntity<Map<String, String>> increaseItem(
            @AuthenticationPrincipal String userId,
            @RequestBody CartItemDocument item
    ) {
        Map<String, String> response = new HashMap<>();
        try {
            cartService.increaseItem(userId, item.getItemIsbn(), item.getQuantity());
            response.put("message", "Item quantity increased successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to increase item quantity.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/decreaseItem")
    public ResponseEntity<Map<String, String>> decreaseItem(
            @AuthenticationPrincipal String userId,
            @RequestBody CartItemDocument item
    ) {
        Map<String, String> response = new HashMap<>();
        try {
            cartService.decreaseItem2(userId, item.getItemIsbn(), item.getQuantity());
            response.put("message", "Item quantity decreased successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to decrease item quantity.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

*/
}

