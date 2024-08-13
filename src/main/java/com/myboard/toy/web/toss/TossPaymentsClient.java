package com.myboard.toy.web.toss;

import org.json.simple.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "tossPaymentsClient", url = "https://api.tosspayments.com")
public interface TossPaymentsClient {

    @PostMapping("/v1/payments/confirm")
    JSONObject confirmPayment(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody JSONObject paymentDetails
    );
}
