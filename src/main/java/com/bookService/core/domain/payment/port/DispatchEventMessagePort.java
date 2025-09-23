package com.bookService.core.domain.payment.port;

import com.bookService.core.domain.payment.PaymentEventMessage;

public interface DispatchEventMessagePort {

    void dispatch(PaymentEventMessage message);

}