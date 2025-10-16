package com.bookService.core.test.cart.mysql.controller;

import com.bookService.core.domain.cart.Cart;
import com.bookService.core.domain.cart.dto.CartListResponseDTO;
import com.bookService.core.domain.cart.service.CartService;
import com.bookService.core.domain.cartitem.CartItem;
import com.bookService.core.domain.cartitem.dto.*;
import com.bookService.core.domain.cartitem.service.CartItemService;
import com.bookService.core.domain.login.dto.AccountDTO;
import com.bookService.core.common.dto.ResponseDTO;
import com.bookService.core.domain.login.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/mysql/cart/test")
public class CartMySqlControllerForDev {
    private final AccountService accountService;
    private final CartService cartService;
    private final CartItemService cartItemService;

    /** 카트에 아이템 추가 */
    @PostMapping("/add")
    public ResponseEntity<?> addItemToCart(
            @AuthenticationPrincipal String userId,
            @RequestParam("isbn") String isbn,
            @RequestParam("amount") int amount
    ) {

        CartItemUpdateRequestForm form = CartItemUpdateRequestForm.builder()
                .userId(userId)
                .isbn(isbn)
                .amount(amount)
                .build();

        CartItem cartItem = cartItemService.createCartItemOrIncreaseAmount(form);
        cartItemService.saveCartItem(cartItem);

        CartItemDTO responseDto = new CartItemDTO(cartItem.getId(), isbn, cartItem.getAmount());

        ResponseDTO<CartItemDTO> response = ResponseDTO.<CartItemDTO>builder().data(List.of(responseDto)).build();

        return ResponseEntity.ok(response);
    }

    // 장바구니 목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> getCartList(@AuthenticationPrincipal String userId) {

        try {
            AccountDTO account = accountService.getAccountById(Long.parseLong(userId));
            if (account == null) {
                throw new RuntimeException("Invalid user");
            }

            List<CartListDTOForQueryProjection> cartList = cartService.getCartItemList(userId);
            CartTotalPriceDto cartTotalPrice = cartService.getCartTotalPrice(account);

            CartListResponseDTO responseData = new CartListResponseDTO(cartList, cartTotalPrice);
            ResponseDTO<CartListResponseDTO> response = ResponseDTO.<CartListResponseDTO>builder().data(List.of(responseData)).build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            ResponseDTO<CartListResponseDTO> response = ResponseDTO.<CartListResponseDTO>builder().error(e.getMessage()).build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    /* 증가 (덮어쓰기 → 가산 방식) */
    @PostMapping("/increaseItem")
    public ResponseEntity<Map<String, String>> increaseAmount(
            @AuthenticationPrincipal String userId,
            @RequestBody CartItemUpdateAmountRequestForm form
    ) {
        Map<String, String> response = new HashMap<>();
        try {
            Long accountId = Long.parseLong(userId);
            Cart cart = cartService.findCartByAccountIdLongType(accountId);

            cartItemService.updateCartItemAmountAdditive(cart, form);

            response.put("message", "Item quantity increased successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Failed to update item quantity.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

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

    /* 초기화 */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, String>> initializeCart(@AuthenticationPrincipal String userId) {
        Map<String, String> response = new HashMap<>();
        try {
            cartItemService.clearCart(userId);
            response.put("message", "Cart initialized successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to initialize cart.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
