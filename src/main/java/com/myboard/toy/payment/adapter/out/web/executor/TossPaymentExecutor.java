package com.myboard.toy.payment.adapter.out.web.executor;

import com.myboard.toy.web.toss.TossPaymentsClient;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class TossPaymentExecutor {

    private final TossPaymentsClient tossPaymentsClient;

    public TossPaymentExecutor(TossPaymentsClient tossPaymentsClient) {
        this.tossPaymentsClient = tossPaymentsClient;
    }
    public JSONObject execute(String paymentKey, String orderId, String amount){
        //Request Body 구성
        JSONObject paymentDetails = new JSONObject();
        paymentDetails.put("paymentKey",paymentKey);
        paymentDetails.put("orderId",orderId);
        paymentDetails.put("amount", amount);

        return tossPaymentsClient.confirmPayment(
                "Basic " + "{encoded-secret-key}",  // 인증 헤더
                "application/json",
                paymentDetails
        );
    }
}
