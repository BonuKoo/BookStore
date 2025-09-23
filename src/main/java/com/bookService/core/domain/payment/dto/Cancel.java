package com.bookService.core.domain.payment.dto;

import lombok.Data;

@Data
public class Cancel {
    private int cancelAmount;
    private String cancelReason;
    private int taxFreeAmount;
    private int taxExemptionAmount;
    private int refundableAmount;
    private int easyPayDiscountAmount;
    private String canceledAt;
    private String transactionKey;
    private String receiptKey;
    private boolean isPartialCancelable;
}

