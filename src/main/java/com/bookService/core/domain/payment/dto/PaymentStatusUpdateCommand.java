package com.bookService.core.domain.payment.dto;


import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;

@Data
@Builder
public class PaymentStatusUpdateCommand {

    private String paymentKey;
    private String orderId;
    private PaymentStatus status;
    private PaymentExtraDetails extraDetails;
    private PaymentFailure failure;

    public PaymentStatusUpdateCommand(
            String paymentKey,
            String orderId,
            PaymentStatus status,
            PaymentExtraDetails extraDetails,
            PaymentFailure failure
    ) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.status = status;
        this.extraDetails = extraDetails;
        this.failure = failure;

        if (status != PaymentStatus.SUCCESS &&
                status != PaymentStatus.FAILURE &&
                status != PaymentStatus.UNKNOWN) {
            throw new IllegalArgumentException("결제 상태 (status: " + status + ") 는 올바르지 않은 결제 상태입니다.");
        }

        if (status == PaymentStatus.SUCCESS) {
            Objects.requireNonNull(extraDetails, "PaymentStatus 값이 SUCCESS 라면 PaymentExtraDetails 는 null 이 되면 안됩니다.");
        } else if (status == PaymentStatus.FAILURE) {
            Objects.requireNonNull(failure, "PaymentStatus 값이 FAILURE 라면 PaymentExecutionFailure 는 null 이 되면 안됩니다.");
        }
    }

    public PaymentStatusUpdateCommand(PaymentExecutionResult result) {
        this(
                result.getPaymentKey(),
                result.getOrderId(),
                result.paymentStatus(),
                result.getExtraDetails(),
                result.getFailure()
        );
    }

    public String getPaymentKey() {
        return paymentKey;
    }

    public String getOrderId() {
        return orderId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentExtraDetails getExtraDetails() {
        return extraDetails;
    }

    public PaymentFailure getFailure() {
        return failure;
    }

}

