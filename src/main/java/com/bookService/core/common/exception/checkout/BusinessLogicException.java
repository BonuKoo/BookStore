package com.bookService.core.common.exception.checkout;

public class BusinessLogicException extends RuntimeException {
    public BusinessLogicException(String message, Exception ex) {
        super(message);
    }
}
