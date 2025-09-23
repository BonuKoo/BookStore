package com.bookService.core.domain.payment.dto;

import lombok.Data;

@Data
public class MobilePhone {
    private String customerMobilePhone;
    private String settlementStatus;
    private String receiptUrl;
}
