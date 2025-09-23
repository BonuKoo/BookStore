package com.bookService.core.domain.payment.dto;

import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import lombok.Data;

@Data
public class PaymentExecutionResult {

    private final String paymentKey;
    private final String orderId;
    private final PaymentExtraDetails extraDetails;
    private final PaymentFailure failure;
    private final boolean isSuccess;
    private final boolean isFailure;
    private final boolean isUnknown;
    private final boolean isRetryable;

    public PaymentExecutionResult(String paymentKey,
                                  String orderId,
                                  PaymentExtraDetails extraDetails,
                                  PaymentFailure failure,
                                  boolean isSuccess,
                                  boolean isFailure,
                                  boolean isUnknown,
                                  boolean isRetryable) {

        if (!(isSuccess || isFailure || isUnknown)) {
            throw new IllegalArgumentException("결제 (orderId: " + orderId + ") 는 올바르지 않은 결제 상태입니다.");
        }

        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.extraDetails = extraDetails;
        this.failure = failure;
        this.isSuccess = isSuccess;
        this.isFailure = isFailure;
        this.isUnknown = isUnknown;
        this.isRetryable = isRetryable;
    }

    public PaymentStatus paymentStatus() {
        if (isSuccess) {
            return PaymentStatus.SUCCESS;
        } else if (isFailure) {
            return PaymentStatus.FAILURE;
        } else if (isUnknown) {
            return PaymentStatus.UNKNOWN;
        } else {
            throw new IllegalStateException("결제 (orderId: " + orderId + ") 는 올바르지 않은 결제 상태입니다.");
        }
    }

}
