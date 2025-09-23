package com.bookService.core.domain.payment.port;

import com.bookService.core.domain.payment.dto.PaymentConfirmCommand;
import com.bookService.core.domain.payment.dto.PaymentExecutionResult;

public interface PaymentExecutorPort {
    PaymentExecutionResult execute(PaymentConfirmCommand command);

}
