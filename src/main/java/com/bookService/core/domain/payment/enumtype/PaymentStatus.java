package com.bookService.core.domain.payment.enumtype;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    NOT_STARTED("결제 시작 "),
    EXECUTING("결제 중"),
    FAILURE("결제 승인 실패"),
    SUCCESS("결제 승인 성공"),
    UNKNOWN("결제 승인 알 수 없는 상태");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentStatus get(String status) {
        for (PaymentStatus paymentStatus : values()) {
            if (paymentStatus.name().equals(status)) {
                return paymentStatus;
            }
        }
        throw new IllegalArgumentException("PaymentStatus: " + status + " 는 올바르지 않은 결제 타입입니다.");
    }

}
