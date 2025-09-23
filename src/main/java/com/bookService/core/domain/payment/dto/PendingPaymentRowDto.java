package com.bookService.core.domain.payment.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PendingPaymentRowDto {

    private Long paymentEventId;
    private String paymentKey;
    private String orderId;
    private Long paymentOrderId;
    private String paymentOrderStatus;
    private BigDecimal amount;
    private Byte failedCount;
    private Byte threshold;

    @QueryProjection
    public PendingPaymentRowDto(Long paymentEventId, String paymentKey, String orderId,
                                Long paymentOrderId, String paymentOrderStatus,
                                BigDecimal amount, Byte failedCount, Byte threshold) {
        this.paymentEventId = paymentEventId;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.paymentOrderId = paymentOrderId;
        this.paymentOrderStatus = paymentOrderStatus;
        this.amount = amount;
        this.failedCount = failedCount;
        this.threshold = threshold;
    }

}
