package com.bookService.core.infra.toss.controller;

import com.bookService.core.domain.payment.dto.PaymentConfirmCommand;
import com.bookService.core.domain.payment.dto.PaymentConfirmationResult;
import com.bookService.core.infra.toss.dto.ApiResponse;
import com.bookService.core.infra.toss.dto.TossPaymentConfirmRequest;
import com.bookService.core.infra.toss.usecase.PaymentConfirmUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/toss")
@RestController
@RequiredArgsConstructor
@Slf4j
public class TossPaymentController {

    private final PaymentConfirmUseCase paymentConfirmUseCase;

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PaymentConfirmationResult>> confirm(@RequestBody TossPaymentConfirmRequest request) {
        PaymentConfirmCommand command = PaymentConfirmCommand.builder()
                .paymentKey(request.getPaymentKey())
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .build();

        // 예외는 GlobalExceptionHandler(@RestControllerAdvice)에 위임한다.
        // (중복 결제 등 도메인 예외를 컨트롤러가 500으로 뭉개지 않도록 자체 try/catch 제거)
        log.info("Calling confirm use case with command: {}", command);
        PaymentConfirmationResult result = paymentConfirmUseCase.confirm(command);
        log.info("Payment confirmation result: {}", result);
        return ResponseEntity.ok(ApiResponse.with(HttpStatus.OK, "", result));
    }

}
