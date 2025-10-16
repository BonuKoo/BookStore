package com.bookService.core.common.exception.checkout;

// HTTP 409 CONFLICT
public class CheckoutAlreadyExistsException extends RuntimeException {
    public CheckoutAlreadyExistsException(String message) {
        super(message);
    }
}
