package com.bookService.core.test.cart.mongo.controller;

import com.bookService.core.test.cart.mongo.document.CartItemRequest;
import com.bookService.core.test.cart.mongo.service.CartMongoServiceTest1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
/*
@RestController
@RequestMapping("/mongo/cart1")
@RequiredArgsConstructor*/
public class CartMongoControllerTest1 {

    /*
    private final CartMongoServiceTest1 cartMongoServiceTest1;

    @PostMapping("/add")
    public ResponseEntity<?> addItem(
            @AuthenticationPrincipal String userId,
            @RequestBody CartItemRequest request
    ) {
        cartMongoServiceTest1.addOrIncrease(userId, request.getItemIsbn(), request.getQuantity());
        return ResponseEntity.ok(Map.of("message", "Item added successfully"));
    }

    @PostMapping("/increase")
    public ResponseEntity<?> increaseItem(
            @AuthenticationPrincipal String userId,
            @RequestBody CartItemRequest request
    ) {
        cartMongoServiceTest1.addOrIncrease(userId, request.getItemIsbn(), request.getQuantity());
        return ResponseEntity.ok(Map.of("message", "Item decreased successfully"));
    }

    @PostMapping("/decrease")
    public ResponseEntity<?> decreaseItem(
            @AuthenticationPrincipal String userId,
            @RequestBody CartItemRequest request
    ) {
        cartMongoServiceTest1.decrease(userId, request.getItemIsbn(), request.getQuantity());
        return ResponseEntity.ok(Map.of("message", "Item decreased successfully"));
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeItem(
            @AuthenticationPrincipal String userId,
            @RequestBody CartItemRequest request
    ) {
        cartMongoServiceTest1.removeItem(userId, request.getItemIsbn());
        return ResponseEntity.ok(Map.of("message", "Item removed successfully"));
    }

    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(cartMongoServiceTest1.getCart(userId));
    }

    @PostMapping("/initialize")
    public ResponseEntity<Map<String, String>> initializeCart(@AuthenticationPrincipal String userId) {
        Map<String, String> response = new HashMap<>();
        try {
            cartMongoServiceTest1.clearCart(userId);
            response.put("message", "Mongo Cart initialized successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to initialize Mongo cart.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }*/
}