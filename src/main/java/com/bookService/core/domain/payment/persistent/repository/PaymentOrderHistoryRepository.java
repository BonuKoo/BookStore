package com.bookService.core.domain.payment.persistent.repository;

import com.bookService.core.domain.payment.entity.PaymentOrderHistory;

import java.util.List;

public interface PaymentOrderHistoryRepository {

    void saveAll(List<PaymentOrderHistory> histories);

}