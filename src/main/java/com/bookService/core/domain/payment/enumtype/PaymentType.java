package com.bookService.core.domain.payment.enumtype;

public enum PaymentType {
    NORMAL("일반 결제"),
    UNKNOWN("알 수 없음");

    private final String description;

    PaymentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentType get(String type) {
        for (PaymentType value : PaymentType.values()) {
            if (value.name().equalsIgnoreCase(type)) {
                return value;
            }
        }
        return UNKNOWN; // 예외 대신 기본값 반환
    }
}

