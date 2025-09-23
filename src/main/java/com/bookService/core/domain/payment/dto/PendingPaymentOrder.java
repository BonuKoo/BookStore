package com.bookService.core.domain.payment.dto;

import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PendingPaymentOrder {

    private Long paymentOrderId;
    private PaymentStatus status;
    private Long amount;
    private int failedCount;
    private int threshold;

}
