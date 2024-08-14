package com.myboard.toy.payment.adapter.in.web.request;

import lombok.Data;

@Data
public class TossPaymentConfirmRequest {

    String paymentKey;
    String orderId;
    Long amount;

}
