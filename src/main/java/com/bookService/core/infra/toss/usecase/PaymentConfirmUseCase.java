package com.bookService.core.infra.toss.usecase;

import com.bookService.core.domain.payment.dto.PaymentConfirmCommand;
import com.bookService.core.domain.payment.dto.PaymentConfirmationResult;

public interface PaymentConfirmUseCase {

    PaymentConfirmationResult confirm(PaymentConfirmCommand command);

}
