package com.bookService.core.domain.payment.port;

import com.bookService.core.domain.payment.dto.PaymentEventDto;

public interface CompletePaymentPort {

    void complete(PaymentEventDto paymentEvent);

    // M4: 메시지 큐 연동 (settlement-worker/ledger-worker의 완결 통지 수신)
    void handleWalletUpdate(PaymentEventDto paymentEvent);
    void handleLedgerUpdate(PaymentEventDto paymentEvent);
}