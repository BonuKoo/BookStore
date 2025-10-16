package com.bookService.core.test.item.exception;

public class StockUnderflowException extends RuntimeException {
    public StockUnderflowException(String message) {
        super(message);
    }
}
