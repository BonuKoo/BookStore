package com.bookService.core.domain.checkout.service;

import com.bookService.core.domain.payment.dto.PaymentCheckoutOptDtoForQueryProjection;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentEventRepository;
import com.bookService.core.domain.payment.usecase.CheckoutFindExistingOrderUseCase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutFindExistingOrderService implements CheckoutFindExistingOrderUseCase {
    private final SpringDataJpaPaymentEventRepository paymentEventRepository;

    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<PaymentCheckoutOptDtoForQueryProjection> findExistingOrder(String orderId) {
        entityManager.clear();
        Optional<PaymentCheckoutOptDtoForQueryProjection> result = paymentEventRepository.findPaymentOptByOrderID(orderId);
        return result;
    }
}
