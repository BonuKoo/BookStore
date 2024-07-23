package com.myboard.toy.common.exception;

public class OrderNotFoundException extends RuntimeException{

    public OrderNotFoundException() {
        super("해당하는 상품은 존재하지 않습니다.");
    }

    public OrderNotFoundException(String message) {
        super(message);
    }

}
