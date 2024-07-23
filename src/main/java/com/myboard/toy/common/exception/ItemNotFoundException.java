package com.myboard.toy.common.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException() {
        super("해당하는 상품은 존재하지 않습니다.");
    }

    public ItemNotFoundException(String message) {
        super(message);
    }
}