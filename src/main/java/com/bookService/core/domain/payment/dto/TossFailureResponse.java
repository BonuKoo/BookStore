package com.bookService.core.domain.payment.dto;

import lombok.Data;

@Data
public class TossFailureResponse {
    private String code;
    private String message;
}