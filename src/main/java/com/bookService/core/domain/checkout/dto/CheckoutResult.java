package com.bookService.core.domain.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutResult {

    private Long amount;
    private String orderId;
    private String orderName;

}

