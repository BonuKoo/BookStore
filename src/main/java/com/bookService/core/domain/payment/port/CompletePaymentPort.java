package com.bookService.core.domain.payment.port;

import com.bookService.core.domain.payment.dto.PaymentEventDto;

public interface CompletePaymentPort {

    void complete(PaymentEventDto paymentEvent);

}