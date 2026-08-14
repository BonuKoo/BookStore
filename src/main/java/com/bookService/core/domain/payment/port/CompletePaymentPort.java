package com.bookService.core.domain.payment.port;

import com.bookService.core.domain.payment.dto.PaymentEventDto;

public interface CompletePaymentPort {

    void complete(PaymentEventDto paymentEvent);

    /**
     * wallet/ledger 완결 플래그가 DB 기준으로 모두 반영됐을 때만 원자적으로
     * is_payment_done을 true로 전환한다. 정확히 한 번만 true를 반환한다.
     */
    boolean tryMarkPaymentDone(String orderId);

    // M4: 메시지 큐 연동 (settlement-worker/ledger-worker의 완결 통지 수신)
    void handleWalletUpdate(PaymentEventDto paymentEvent);
    void handleLedgerUpdate(PaymentEventDto paymentEvent);
}