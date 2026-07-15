package com.bookService.core.domain.payment.outbox;

public enum OutboxStatus {

    /** 저장만 되고 아직 발행되지 않은 초기 상태 */
    INIT,
    /** 브로커 발행 성공 */
    SUCCESS,
    /** 발행 시도했으나 실패 — 릴레이 스케줄러의 재발행 대상 */
    FAILURE
}
