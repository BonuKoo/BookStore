package com.bookService.core.domain.payment.port;

public interface PaymentValidationPort {
    Boolean isValid(String orderId, Long amount);

}
