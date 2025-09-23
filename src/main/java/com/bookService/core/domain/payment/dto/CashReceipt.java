package com.bookService.core.domain.payment.dto;


import lombok.Data;

@Data
public class CashReceipt {
    private String type;
    private String receiptKey;
    private String issueNumber;
    private String receiptUrl;
    private int amount;
    private int taxFreeAmount;
}
