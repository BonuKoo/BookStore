package com.bookService.core.domain.checkout.dto;

import com.bookService.core.domain.checkout.enumType.CheckoutStatus;
import com.bookService.core.domain.payment.dto.PaymentCheckoutOptDtoForQueryProjection;
import com.bookService.core.domain.payment.entity.PaymentEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CheckoutResult {

    private Long amount;
    private String orderId;
    private String orderName;
    private CheckoutStatus status;

    public CheckoutResult(Long amount, String orderId, String orderName) {
        this.amount = amount;
        this.orderId = orderId;
        this.orderName = orderName;
    }

    public static CheckoutResult created(PaymentEvent event) {
        return CheckoutResult.builder()
                .orderId(event.getOrderId())
                .orderName(event.getOrderName())
                .amount(event.totalAmount())
                .status(CheckoutStatus.SUCCESS)
                .build();
    }

    public static CheckoutResult alreadyExists(PaymentEvent event) {
        return CheckoutResult.builder()
                .orderId(event.getOrderId())
                .orderName(event.getOrderName())
                .amount(event.totalAmount())
                .status(CheckoutStatus.ALREADY_EXISTS)
                .build();
    }

    public static CheckoutResult alreadyExists2(PaymentCheckoutOptDtoForQueryProjection dto) {
        return CheckoutResult.builder()
                .orderId(dto.getOrderId())
                .orderName(dto.getOrderName())
                .amount(dto.getTotalAmount())
                .status(CheckoutStatus.ALREADY_EXISTS)
                .build();
    }

    public static CheckoutResult failed() {
        return CheckoutResult.builder()
                .status(CheckoutStatus.FAILED)
                .build();
    }
}

