package com.bookService.core.test.cart.mysql.service;

import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentEventRepository;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentOrderHistoryRepository;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCleanupService {

    private final SpringDataJpaPaymentOrderHistoryRepository historyRepository;
    private final SpringDataJpaPaymentOrderRepository orderRepository;
    private final SpringDataJpaPaymentEventRepository eventRepository;

    @Transactional
    public void deleteAllPayments() {
        historyRepository.deleteAllInBatch(); //
        orderRepository.deleteAllInBatch();   //
        eventRepository.deleteAllInBatch();   //
    }
}
