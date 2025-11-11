package com.bookService.core.common.exception.checkout;

public class BusinessLogicException extends RuntimeException {

    private final CheckoutErrorCode errorCode;

    // 에러 코드만 받는 생성자
    public BusinessLogicException(CheckoutErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 에러 코드와 원인 예외를 함께 받는 생성자
    public BusinessLogicException(CheckoutErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public CheckoutErrorCode getErrorCode() {
        return errorCode;
    }
}
