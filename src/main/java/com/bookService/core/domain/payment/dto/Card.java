package com.bookService.core.domain.payment.dto;

import lombok.Data;

@Data
public class Card {
    private int amount;
    private String issuerCode;
    private String acquirerCode;
    private String number;
    private int installmentPlanMonths;
    private String approveNo;
    private boolean useCardPoint;
    private String cardType;
    private String ownerType;
    private String acquireStatus;
    private boolean isInterestFree;
    private String interestPayer;
}

