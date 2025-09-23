package com.bookService.core.domain.payment.port;

import com.bookService.core.domain.payment.entity.PaymentEvent;

public interface SavePaymentPort {
    void save(PaymentEvent paymentEvent);

}
