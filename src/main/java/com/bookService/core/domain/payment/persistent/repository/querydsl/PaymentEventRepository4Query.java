package com.bookService.core.domain.payment.persistent.repository.querydsl;

import com.bookService.core.domain.payment.dto.PaymentCheckoutOptDtoForQueryProjection;
import com.bookService.core.domain.payment.dto.PaymentEventDto;
import com.bookService.core.domain.payment.dto.PendingPaymentRowDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentEventRepository4Query {

    List<PendingPaymentRowDto> findPendingPaymentRows(LocalDateTime now);
    PaymentEventDto getPaymentEventAndOrders(String orderId);

    void handlePaymentCompletion(PaymentEventDto paymentEventDto);

    /**
     * wallet/ledger 완결 플래그가 모두 반영된 경우에만 원자적으로 is_payment_done을 true로
     * 전환한다. 두 통지가 병렬로 도착해도 in-memory 스냅샷이 아니라 DB의 현재 상태를
     * 조건으로 재확인하므로, 정확히 한 번만 true를 반환한다(레이스 컨디션 방지).
     */
    boolean tryMarkPaymentDone(String orderId);

    Optional<PaymentCheckoutOptDtoForQueryProjection> findPaymentOptByOrderID(String orderId);

    // M4: 메시지 큐 연동 (settlement-worker/ledger-worker의 완결 통지 수신)
    void handleWalletUpdate(PaymentEventDto paymentEventDto);
    void handleLedgerUpdate(PaymentEventDto paymentEventDto);
}
