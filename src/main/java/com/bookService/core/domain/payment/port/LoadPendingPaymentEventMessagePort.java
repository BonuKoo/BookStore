package com.bookService.core.domain.payment.port;

import com.bookService.core.domain.payment.PaymentEventMessage;

import java.util.List;

public interface LoadPendingPaymentEventMessagePort {
    List<PaymentEventMessage> getPendingPaymentEventMessage();
}

/**
 * 전송이 되지 않은 이벤트 메시지들을 갖고 오는 포트
 */