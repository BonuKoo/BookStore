package com.bookService.core.domain.payment.dto;


import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class PaymentConfirmationResult {

    private final PaymentStatus status;
    private final PaymentFailure failure;
    private final String message;

    public PaymentConfirmationResult(PaymentStatus status, PaymentFailure failure) {
        if (status == PaymentStatus.FAILURE && failure == null) {
            throw new IllegalArgumentException("결제 상태 FAILURE 일 때 PaymentFailure 는 null 값이 될 수 없습니다.");
        }

        this.status = status;
        this.failure = failure;
        this.message = generateMessage(status);
    }

    private String generateMessage(PaymentStatus status) {
        switch (status) {
            case SUCCESS:
                return "결제 처리에 성공하였습니다.";
            case FAILURE:
                return "결제 처리에 실패하였습니다.";
            case UNKNOWN:
                return "결제 처리 중에 알 수 없는 에러가 발생하였습니다.";
            default:
                throw new IllegalArgumentException("현재 결제 상태 (status: " + status + ") 는 올바르지 않은 상태입니다.");
        }
    }
}
