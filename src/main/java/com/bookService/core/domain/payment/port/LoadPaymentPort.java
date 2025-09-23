package com.bookService.core.domain.payment.port;

import com.bookService.core.domain.payment.dto.PaymentEventDto;
import com.bookService.core.domain.payment.entity.PaymentEvent;

public interface LoadPaymentPort {
    PaymentEvent getPayment(String orderId);
    PaymentEventDto getPaymentEventAndOrders(String orderId);
}
