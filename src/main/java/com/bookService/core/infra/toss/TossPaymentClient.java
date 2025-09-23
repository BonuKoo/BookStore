package com.bookService.core.infra.toss;

import com.bookService.core.infra.toss.dto.TossPaymentConfirmRequest;
import com.bookService.core.infra.toss.dto.TossPaymentConfirmationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "tossPaymentClient", url = "https://api.tosspayments.com")
public interface TossPaymentClient {

    @PostMapping("/v1/payments/confirm")
    public TossPaymentConfirmationResponse confirmPayment(
            @RequestHeader("Idempotency-Key") String orderId,
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody TossPaymentConfirmRequest paymentConfirmRequest
    );
}
