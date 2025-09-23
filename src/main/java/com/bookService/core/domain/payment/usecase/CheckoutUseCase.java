package com.bookService.core.domain.payment.usecase;

import com.bookService.core.domain.checkout.dto.CheckoutCommand;
import com.bookService.core.domain.checkout.dto.CheckoutResult;

public interface CheckoutUseCase {
    CheckoutResult checkout(CheckoutCommand command);

    CheckoutResult checkout2(CheckoutCommand command);
}
