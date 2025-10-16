package com.bookService.core.domain.payment.usecase;

import com.bookService.core.domain.payment.dto.PaymentCheckoutOptDtoForQueryProjection;

import java.util.Optional;

public interface CheckoutFindExistingOrderUseCase {
    Optional<PaymentCheckoutOptDtoForQueryProjection> findExistingOrder(String orderId);
    }
