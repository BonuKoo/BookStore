package com.bookService.core.domain.payment.persistent.repository.springdata;

import com.bookService.core.domain.payment.entity.PaymentOrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataJpaPaymentOrderHistoryRepository extends JpaRepository<PaymentOrderHistory, Long> {
}
