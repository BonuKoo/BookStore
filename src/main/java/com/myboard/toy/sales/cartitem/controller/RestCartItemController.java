package com.myboard.toy.sales.cartitem.controller;

import com.myboard.toy.sales.cart.service.CartService;
import com.myboard.toy.sales.cartitem.service.CartItemService;
import com.myboard.toy.sales.domain.Cart;
import com.myboard.toy.sales.domain.dto.CartDto;
import com.myboard.toy.sales.domain.dto.CartItemRemoveRequestForm;
import com.myboard.toy.sales.domain.dto.CartItemUpdateAmountRequestForm;
import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class RestCartItemController {

    private final CartItemService cartItemService;
    private final CartService cartService;
    private final UserService userService;

    /*
     UPDATE
     */

    /* 증가 */
    @PostMapping("/increaseItem")
    public ResponseEntity<Map<String, String>> increaseAmountWhenPushTheButton(
            Principal principal,
            @RequestBody CartItemUpdateAmountRequestForm form
    ) {
        Map<String, String> response = new HashMap<>();
        try {

            AccountDto accountId = userService.getAccountIdByPrincipal(principal);

            Cart cart = cartService.findCartByAccountId(accountId);

            form.setAmount(form.getAmount()); // 수량 증가

            cartItemService.updateCartItemAmount(cart, form);

            response.put("message", "Item quantity increased successfully.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Failed to update item quantity.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    /* 감소 */
    @PostMapping("/decreaseItem")
    public ResponseEntity<Map<String, String>> decreaseAmountWhenPushTheButton(
            Principal principal,
            @RequestBody CartItemUpdateAmountRequestForm form
    ) {
        Map<String, String> response = new HashMap<>();
        try {

            AccountDto accountId = userService.getAccountIdByPrincipal(principal);

            Cart cart = cartService.findCartByAccountId(accountId);

            form.setAmount(Math.max(0, form.getAmount())); // 수량 감소

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
            Principal principal,
            @RequestBody CartItemRemoveRequestForm form) {

        Map<String, String> response = new HashMap<>();
        try {
            // 현재 인증된 사용자로부터 accountId를 가져옴
            AccountDto accountId = userService.getAccountIdByPrincipal(principal);

            // accountId로 Cart를 찾아서 cartId를 가져옴
            CartDto cartDto = cartService.getCartIdByAccountId(accountId);
            Long cartId = cartDto.getId();

            // 요청으로 받은 form에 cartId를 설정
            form.setCartId(cartId);

            // 서비스 호출하여 아이템 제거
            cartItemService.removeCartItem(form);

            // 성공적인 응답 반환 (200 OK와 함께 JSON 메시지)
            response.put("message", "Item removed successfully.");
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            // 존재하지 않는 경우 404 오류와 함께 메시지 반환
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            // 일반적인 오류의 경우 500 오류와 함께 메시지 반환
            response.put("error", "Failed to remove item.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}