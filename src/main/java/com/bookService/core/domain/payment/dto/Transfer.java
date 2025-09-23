package com.bookService.core.domain.payment.dto;

import lombok.Data;

@Data
public class Transfer {
    private String bankCode;
    private String settlementStatus;
}