package com.bookService.core.domain.payment.persistent.repository.querydsl;

import com.bookService.core.domain.payment.dto.PaymentEventDto;
import com.bookService.core.domain.payment.dto.PendingPaymentRowDto;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentEventRepository4Query {

    List<PendingPaymentRowDto> findPendingPaymentRows(LocalDateTime now);
    PaymentEventDto getPaymentEventAndOrders(String orderId);

    void handlePaymentCompletion(PaymentEventDto paymentEventDto);
    /*
    void handleWalletUpdate(PaymentEventDto paymentEventDto);
    void handleLedgerUpdate(PaymentEventDto paymentEventDto);
    */
}
