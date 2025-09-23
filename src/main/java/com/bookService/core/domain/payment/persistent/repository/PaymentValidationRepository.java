package com.bookService.core.domain.payment.persistent.repository;

public interface PaymentValidationRepository {
    boolean isValid(String orderId, long amount);
}
