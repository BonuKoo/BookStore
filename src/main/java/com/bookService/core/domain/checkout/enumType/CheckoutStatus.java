package com.bookService.core.domain.checkout.enumType;

public enum CheckoutStatus {

    SUCCESS("결제가 정상 생성되었습니다."),        // 정상 신규 결제 생성
    ALREADY_EXISTS("이미 요청한 주문입니다."), // 이미 동일 orderId가 존재
    FAILED("주문에 실패했습니다.");

    private final String description;

    CheckoutStatus(String description) {this.description = description;}
}
