package com.bookService.core.domain.payment.dto;


import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class PaymentCheckoutOptDtoForQueryProjection {
    private String orderId;
    private String orderName;
    private Long totalAmount;

    @QueryProjection
    public PaymentCheckoutOptDtoForQueryProjection(String orderId, String orderName, Long totalAmount) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.totalAmount = totalAmount;
    }
}
