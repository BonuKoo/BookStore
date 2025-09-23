package com.bookService.core.domain.payment.persistent.repository;

import com.bookService.core.domain.payment.entity.PaymentOrder;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository {

    Optional<PaymentOrder> findByOrderId(String orderId);

    List<PaymentOrder> findListPaymentOrderByIdempotencyKey(String orderId);

    boolean isValid(String orderId, long amount);

    long incrementFailedCountByOrderId(String orderId);

    void saveAll(List<PaymentOrder> orders);

}
