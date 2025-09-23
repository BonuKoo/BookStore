package com.bookService.core.common.exception;

public class NotEnoughStockException extends RuntimeException {

    public NotEnoughStockException() {
        super("재고가 부족합니다.");
    }

    public NotEnoughStockException(String message) {
        super(message);
    }}
