package com.bookService.core.domain.payment.port;

import com.bookService.core.domain.payment.dto.PendingPaymentEvent;

import java.util.List;

public interface LoadPendingPaymentPort {

    List<PendingPaymentEvent> getPendingPayments();

}