package com.myboard.toy.sales.cart.controller;

import com.myboard.toy.sales.cart.service.CartService;
import com.myboard.toy.sales.cart.service.RedisCartService;
import com.myboard.toy.sales.cartitem.dto.RedisCartDto;
import com.myboard.toy.sales.cartitem.dto.RedisCartItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class RedisCartController {

    private final RedisCartService redisCartService;

    private final CartService cartService;

    /*
        특정 유저 ID를 기반으로 DB에서 장바구니 정보를 가져오고 Redis에 저장
     */

    @GetMapping("/{userId}")
    public ResponseEntity<RedisCartDto> getCart(@PathVariable String userId) {
        RedisCartDto cart = redisCartService.getOrCreateCart(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * 아이템 추가
     */
    @PostMapping("/{userId}/items")
    public ResponseEntity<RedisCartDto> addItemToCart(
            @PathVariable String userId,
            @RequestBody RedisCartItemDto newItem) {
        redisCartService.addItemToCart(userId, newItem);
        RedisCartDto updatedCart = redisCartService.getOrCreateCart(userId);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * 아이템 수량 변경
     */

    @PutMapping("/{userId}/items/{itemId}")
    public ResponseEntity<RedisCartDto> updateItemQuantity(
            @PathVariable String userId,
            @PathVariable String itemId,
            @RequestParam int newCount) {
        redisCartService.updateItemQuantity(userId, itemId, newCount);
        RedisCartDto updatedCart = redisCartService.getOrCreateCart(userId);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * 아이템 삭제
     */
    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<RedisCartDto> removeItemFromCart(
            @PathVariable String userId,
            @PathVariable String itemId) {
        redisCartService.removeItemFromCart(userId, itemId);
        RedisCartDto updatedCart = redisCartService.getOrCreateCart(userId);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * 장바구니 비우기
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<RedisCartDto> clearCart(@PathVariable String userId) {
        redisCartService.clearCart(userId);
        RedisCartDto clearedCart = redisCartService.getOrCreateCart(userId);
        return ResponseEntity.ok(clearedCart);
    }

}
