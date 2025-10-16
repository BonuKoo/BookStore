package com.bookService.core.test.cart.exception;

public class CartItemNotExistException extends RuntimeException {
    public CartItemNotExistException(String message) {
        super(message);
    }
}
