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

    Optional<PaymentCheckoutOptDtoForQueryProjection> findPaymentOptByOrderID(String orderId);

    // M4: 메시지 큐 연동 (settlement-worker/ledger-worker의 완결 통지 수신)
    void handleWalletUpdate(PaymentEventDto paymentEventDto);
    void handleLedgerUpdate(PaymentEventDto paymentEventDto);
}
