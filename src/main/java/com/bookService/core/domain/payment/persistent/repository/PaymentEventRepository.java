package com.bookService.core.domain.payment.persistent.repository;

import com.bookService.core.domain.payment.dto.PaymentEventDto;
import com.bookService.core.domain.payment.dto.PendingPaymentEvent;
import com.bookService.core.domain.payment.dto.PendingPaymentRowDto;
import com.bookService.core.domain.payment.entity.PaymentEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentEventRepository {

    void save (PaymentEvent paymentEvent);

    Optional<PaymentEvent> findByOrderId(String orderId);

    List<PendingPaymentEvent> getPendingPayments();

    List<PendingPaymentRowDto> findPendingPaymentRows(LocalDateTime now);

    // 추후 orderId로 바꿔야 함
    PaymentEvent getPayment(String orderId);

    PaymentEventDto getPaymentEventAndOrders(String orderId);

    void complete(PaymentEventDto paymentEventDto);
}