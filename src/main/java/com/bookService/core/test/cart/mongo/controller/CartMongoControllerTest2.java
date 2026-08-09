package com.bookService.core.test.cart.mongo.controller;

/*
import com.bookService.core.test.cart.mongo.document.CartItemModelArrayDocument2;
import com.bookService.core.test.cart.mongo.document.CartItemRequest;
import com.bookService.core.test.cart.mongo.service.CartMongoServiceTest2;
*/
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/*
@RestController
@RequestMapping("/mongo/cart2")
@RequiredArgsConstructor
*/
public class CartMongoControllerTest2 {
/*
    private final CartMongoServiceTest2 cartMongoServiceTest2;

    @PostMapping("/add")
    public ResponseEntity<?> addItem(
            @AuthenticationPrincipal String userId,
            @RequestBody CartItemRequest request
    ) {
        cartMongoServiceTest2.addItem(userId,
                new CartItemModelArrayDocument2(
                        request.getItemIsbn(), request.getQuantity()
                )
        );
        return ResponseEntity.ok(Map.of("message", "Item added successfully"));
    }

    @PostMapping("/increase")
    public ResponseEntity<?> increaseItem(
            @AuthenticationPrincipal String userId,
            @RequestBody CartItemRequest request
    ) {
        cartMongoServiceTest2.increaseItemSafe2(userId, request.getItemIsbn(), request.getQuantity());
        return ResponseEntity.ok(Map.of("message", "Item increased successfully"));
    }

    @PostMapping("/decrease")
    public ResponseEntity<?> decreaseItem(
            @AuthenticationPrincipal String userId,
            @RequestBody CartItemRequest request
    ) {
        cartMongoServiceTest2.decreaseItemSafe2(userId, request.getItemIsbn(), request.getQuantity());
        return ResponseEntity.ok(Map.of("message", "Item decreased successfully"));
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeItem(
            @AuthenticationPrincipal String userId,
            @RequestBody CartItemRequest request
    ) {
        cartMongoServiceTest2.removeItem(userId, request.getItemIsbn());
        return ResponseEntity.ok(Map.of("message", "Item removed successfully"));
    }

    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(cartMongoServiceTest2.getCart(userId));
    }
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, String>> initializeCart(@AuthenticationPrincipal String userId) {
        Map<String, String> response = new HashMap<>();
        try {
            cartMongoServiceTest2.clearCart(userId);
            response.put("message", "Mongo Cart initialized successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Failed to initialize Mongo cart.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    */
}