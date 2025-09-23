package com.bookService.core.domain.payment.dto;

import lombok.Data;

@Data
public class EasyPay {
    private String provider;
    private int amount;
    private int discountAmount;
}
