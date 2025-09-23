package com.bookService.core.domain.payment.persistent.repository.jpa;


import com.bookService.core.domain.payment.entity.PaymentOrder;
import com.bookService.core.domain.payment.persistent.repository.PaymentOrderRepository;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaPaymentOrderRepository implements PaymentOrderRepository {

    private final SpringDataJpaPaymentOrderRepository springDataJpaPaymentOrderRepository;

    @Override
    public Optional<PaymentOrder> findByOrderId(String orderId) {
        return springDataJpaPaymentOrderRepository.findByOrderId(orderId);
    }

    @Override
    public List<PaymentOrder> findListPaymentOrderByIdempotencyKey(String orderId) {
        return springDataJpaPaymentOrderRepository.findListPaymentOrderByIdempotencyKey(orderId);
    }

    @Override
    public boolean isValid(String orderId, long amount) {
        return springDataJpaPaymentOrderRepository.isValid(orderId, amount);
    }

    @Override
    public long incrementFailedCountByOrderId(String orderId) {
        return springDataJpaPaymentOrderRepository.incrementFailedCountByOrderId(orderId);
    }

    @Override
    public void saveAll(List<PaymentOrder> orders) {
        springDataJpaPaymentOrderRepository.saveAll(orders);
    }
}
