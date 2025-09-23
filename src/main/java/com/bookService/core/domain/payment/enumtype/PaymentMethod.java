package com.bookService.core.domain.payment.enumtype;

public enum PaymentMethod {
    EASY_PAY("간편결제");

    private final String method;

    PaymentMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public static PaymentMethod get(String method) {
        for (PaymentMethod value : PaymentMethod.values()) {
            if (value.method.equals(method)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Payment Method (method: " + method + ") 는 올바르지 않은 결제 방법입니다.");
    }
}
