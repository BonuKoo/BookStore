package com.bookService.core.domain.payment.contoller;

import com.bookService.core.common.util.IdempotencyCreator;
import com.bookService.core.domain.cart.service.CartService;
import com.bookService.core.domain.checkout.dto.CheckoutCommand;
import com.bookService.core.domain.checkout.dto.CheckoutRequest;
import com.bookService.core.domain.checkout.dto.CheckoutResult;
import com.bookService.core.domain.payment.usecase.CheckoutUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class RestCheckoutController {
    private final CartService cartService;
    private final CheckoutUseCase checkoutUseCase;

    @PostMapping
    public ResponseEntity<CheckoutResult> checkout(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        // Cart 조회 (JWT 기반 사용자)

        Long cartId = cartService.findCartByAccountIdStringType(userId);

        CheckoutCommand checkoutCommand = CheckoutCommand.builder()
                .cartId(cartId)
                .buyerId(Long.parseLong(userId))
                .cartItemIds(request.getCartItemIds())
                .idempotencyKey(IdempotencyCreator.create(request))
                .build();

        CheckoutResult result = checkoutUseCase.checkout(checkoutCommand);

        return ResponseEntity.ok(result);
    }
}
