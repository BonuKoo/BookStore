package com.bookService.core.common.exception.checkout;

import org.springframework.http.HttpStatus;

public enum CheckoutErrorCode {

    // 4XX Conflict (동시성 충돌, 중복 데이터)
    CONCURRENCY_CONFLICT(409, "동시성 충돌 발생: 주문 경쟁이 치열합니다. 잠시 후 다시 시도해 주세요."),
    ALREADY_PROCESSED_ORDER(403, "이미 처리된 주문입니다. (데이터 포함 반환)"),

    // 422 Unprocessable Entity (비즈니스 규칙 위반)
    STOCK_UNDERFLOW(422, "재고 부족: 요청하신 수량은 현재 구매할 수 없습니다."),
    INVALID_CART_ITEM(422, "유효하지 않은 장바구니 항목 정보입니다."),

    // 5XX Internal Server Error (예상치 못한 서버 및 DB 오류)
    UNEXPECTED_BUSINESS_ERROR(500, "결제 처리 중 예상치 못한 오류가 발생했습니다."),
    DB_INTEGRITY_VIOLATION(500, "데이터베이스 무결성 제약 조건 위반 오류가 발생했습니다."),
    UNHANDLED_EXCEPTION(500, "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");

    private final int status;
    private final String message;

    CheckoutErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }

    public HttpStatus getHttpStatus() {
        return HttpStatus.valueOf(this.status);
    }

}
