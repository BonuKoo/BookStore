package com.myboard.toy.common.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("해당하는 유저는 존재하지 않습니다.");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}