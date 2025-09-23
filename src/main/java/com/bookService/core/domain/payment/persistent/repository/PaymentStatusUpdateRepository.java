package com.bookService.core.domain.payment.persistent.repository;

import com.bookService.core.domain.payment.dto.PaymentStatusUpdateCommand;

public interface PaymentStatusUpdateRepository {
    Boolean updatePaymentStatusToExecuting(String orderId, String paymentKey);
    Boolean updatePaymentStatus(PaymentStatusUpdateCommand command);
}