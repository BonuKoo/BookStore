package com.bookService.core.domain.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * settlement-worker(M2)/ledger-worker(M3)가 보내는 완결 통지의 공통 형태.
 * 두 워커는 서로 다른 배포 단위(PC3)이므로 클래스를 공유하지 않고 JSON 계약만
 * 맞춘다 — {@code {"payload": {"orderId": "..."}}}.
 */
@Data
@NoArgsConstructor
public class CompletedEventMessage {

    private Map<String, Object> payload;

    public String getOrderId() {
        return String.valueOf(payload.get("orderId"));
    }
}
