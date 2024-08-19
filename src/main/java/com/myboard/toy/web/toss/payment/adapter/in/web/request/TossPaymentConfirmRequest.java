package com.myboard.toy.web.toss.payment.adapter.in.web.request;

import lombok.Data;

@Data
public class TossPaymentConfirmRequest {

    String paymentKey;
    String orderId;
    Long amount;

}
