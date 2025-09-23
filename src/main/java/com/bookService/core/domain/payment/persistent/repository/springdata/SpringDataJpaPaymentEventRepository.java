package com.bookService.core.domain.payment.persistent.repository.springdata;

import com.bookService.core.domain.payment.entity.PaymentEvent;
import com.bookService.core.domain.payment.persistent.repository.querydsl.PaymentEventRepository4Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface SpringDataJpaPaymentEventRepository extends JpaRepository<PaymentEvent, Long>, PaymentEventRepository4Query {

    Optional<PaymentEvent> findByOrderId(String orderId);

    Optional<PaymentEvent> findByOrderName(String orderName);



}
