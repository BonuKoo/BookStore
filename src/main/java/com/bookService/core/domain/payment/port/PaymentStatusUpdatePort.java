package com.bookService.core.domain.payment.port;

import com.bookService.core.domain.payment.dto.PaymentStatusUpdateCommand;

public interface PaymentStatusUpdatePort {
    Boolean updatePaymentStatusToExecuting(String orderId, String paymentKey);
    Boolean updatePaymentStatus(PaymentStatusUpdateCommand command);
}
