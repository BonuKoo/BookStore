package com.bookService.core.domain.checkout.controller;

import com.bookService.core.common.dto.ResponseDTO;
import com.bookService.core.domain.checkout.dto.CheckoutRequest;
import com.bookService.core.domain.checkout.dto.CheckoutResult;
import com.bookService.core.domain.checkout.enumType.CheckoutStatus;
import com.bookService.core.domain.payment.usecase.CheckoutUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class RestCheckoutController {
    private final CheckoutUseCase checkoutUseCase;

    @PostMapping
    public ResponseEntity<?> checkout(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        try {
        CheckoutResult result = checkoutUseCase.checkout(userId, request);

        ResponseDTO<CheckoutResult> responseDTO =
                ResponseDTO.<CheckoutResult>builder()
                        .data(List.of(result))
                        .build();
        if (result.getStatus()== CheckoutStatus.ALREADY_EXISTS){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseDTO);
        }
        return ResponseEntity.ok(responseDTO);

        }catch (Exception e){
            ResponseDTO<CheckoutResult> responseDTO =
                    ResponseDTO.<CheckoutResult>builder()
                            .error(e.getMessage())
                            .build();
            return ResponseEntity.badRequest().body(responseDTO);
        }
    }
}