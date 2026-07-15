package com.bookService.core.domain.payment.adapter;


import com.bookService.core.domain.payment.PaymentEventMessage;
import com.bookService.core.domain.payment.dto.PaymentEventDto;
import com.bookService.core.domain.payment.dto.PaymentStatusUpdateCommand;
import com.bookService.core.domain.payment.dto.PendingPaymentEvent;
import com.bookService.core.domain.payment.entity.PaymentEvent;
import com.bookService.core.domain.payment.persistent.PaymentStatusUpdateRepository;
import com.bookService.core.domain.payment.persistent.repository.PaymentEventRepository;
import com.bookService.core.domain.payment.persistent.repository.PaymentOrderHistoryRepository;
import com.bookService.core.domain.payment.persistent.repository.PaymentOrderRepository;
import com.bookService.core.domain.payment.port.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentPersistentAdapter implements SavePaymentPort, PaymentStatusUpdatePort, PaymentValidationPort,
        LoadPendingPaymentPort,
        //LoadPendingPaymentEventMessagePort,
        LoadPaymentPort, CompletePaymentPort {

    private final PaymentEventRepository paymentEventRepository;
    private final PaymentStatusUpdateRepository paymentStatusUpdateRepository;
    private final PaymentOrderHistoryRepository paymentOrderHistoryRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    //private final PaymentOutboxRepository paymentOutboxRepository;

    @Override
    public void save(PaymentEvent paymentEvent) {
        paymentEventRepository.save(paymentEvent);
    }
    @Override
    public Boolean updatePaymentStatusToExecuting(String orderId, String paymentKey) {
        return paymentStatusUpdateRepository.updatePaymentStatusToExecuting(orderId,paymentKey);
    }
    @Override
    public Boolean isValid(String orderId, Long amount) {
        return paymentOrderRepository.isValid(orderId,amount);
    }
    @Override
    public Boolean updatePaymentStatus(PaymentStatusUpdateCommand command) {
        return paymentStatusUpdateRepository.updatePaymentStatus(command);
    }
    @Override
    public List<PendingPaymentEvent> getPendingPayments() {
        return paymentEventRepository.getPendingPayments();
    }

    //@Override
    /*
    public List<PaymentEventMessage> getPendingPaymentEventMessage() {
        return paymentOutboxRepository.findPendingPaymentOutboxes();
    }*/

    // todo 추후 orderName -> orderId로 변경
    @Override
    public PaymentEvent getPayment(String orderName) {
        return paymentEventRepository.getPayment(orderName);
    }

    @Override
    public PaymentEventDto getPaymentEventAndOrders(String orderId) {
        return paymentEventRepository.getPaymentEventAndOrders(orderId);
    }

    @Override
    public void complete(PaymentEventDto paymentEvent) {
        paymentEventRepository.complete(paymentEvent);
    }

    @Override
    public void handleWalletUpdate(PaymentEventDto paymentEvent) {
        paymentEventRepository.handleWalletUpdate(paymentEvent);
    }

    @Override
    public void handleLedgerUpdate(PaymentEventDto paymentEvent) {
        paymentEventRepository.handleLedgerUpdate(paymentEvent);
    }

}
