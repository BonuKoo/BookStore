package com.bookService.core.domain.cart.controller;

import com.bookService.core.domain.cart.dto.CartListResponseDTO;
import com.bookService.core.domain.cart.service.CartService;
import com.bookService.core.domain.cartitem.CartItem;
import com.bookService.core.domain.cartitem.dto.CartItemDTO;
import com.bookService.core.domain.cartitem.dto.CartItemUpdateRequestForm;
import com.bookService.core.domain.cartitem.dto.CartListDTOForQueryProjection;
import com.bookService.core.domain.cartitem.dto.CartTotalPriceDto;
import com.bookService.core.domain.cartitem.service.CartItemService;
import com.bookService.core.domain.item.service.ItemService;
import com.bookService.core.domain.login.dto.AccountDTO;
import com.bookService.core.common.dto.ResponseDTO;
import com.bookService.core.domain.login.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class RestCartController {

    private final CartService cartService;
    private final CartItemService cartItemService;
    private final ItemService itemService;
    private final AccountService userService; // Account 조회용

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
            AccountDTO account = userService.getAccountById(Long.parseLong(userId));
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
}
