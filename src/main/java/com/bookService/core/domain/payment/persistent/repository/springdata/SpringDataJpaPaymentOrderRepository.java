package com.bookService.core.domain.payment.persistent.repository.springdata;

import com.bookService.core.domain.payment.entity.PaymentOrder;
import com.bookService.core.domain.payment.persistent.repository.querydsl.PaymentOrderRepository4Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface SpringDataJpaPaymentOrderRepository extends JpaRepository<PaymentOrder, Long>, PaymentOrderRepository4Query {

    Optional<PaymentOrder> findByOrderId(String orderId);

}