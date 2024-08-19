package com.myboard.toy.web.toss.client;

import com.myboard.toy.web.toss.payment.adapter.out.web.config.TossWebClientConfiguration;
import org.json.simple.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(url = "${toss.api.url}",
        name = "tossPaymentsClient",
        configuration = TossWebClientConfiguration.class)
public interface TossPaymentsClient {

    @PostMapping("/v2/payments/confirm")
    JSONObject confirmPayment(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody JSONObject paymentDetails
    );
}
