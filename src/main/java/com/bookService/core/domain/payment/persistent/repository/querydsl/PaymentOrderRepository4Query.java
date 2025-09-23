package com.bookService.core.domain.payment.persistent.repository.querydsl;

import com.bookService.core.domain.payment.entity.PaymentOrder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentOrderRepository4Query{

    List<PaymentOrder> findListPaymentOrderByIdempotencyKey(String orderId);

    boolean isValid(String orderId, long amount);

    long incrementFailedCountByOrderId(String orderId);
}

// 전용 DTO 만들어서 QueryProjection 생성자