package com.bookService.core.domain.payment.dto;

import lombok.Data;

@Data
public class VirtualAccount {

    private String accountType;
    private String accountNumber;
    private String bankCode;
    private String customerName;
    private String dueDate;
    private String refundStatus;
    private boolean expired;
    private String settlementStatus;
    private RefundReceiveAccount refundReceiveAccount;
    private String secret;

}

