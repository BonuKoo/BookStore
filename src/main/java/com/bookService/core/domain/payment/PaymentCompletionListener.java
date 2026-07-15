package com.bookService.core.domain.payment;

import com.bookService.core.config.rabbitmq.RabbitMqConfig;
import com.bookService.core.domain.payment.dto.CompletedEventMessage;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompletionListener {

    private final PaymentCompletionService paymentCompletionService;

    @RabbitListener(queues = RabbitMqConfig.SETTLEMENT_WALLET_COMPLETED_QUEUE)
    public void onWalletCompleted(CompletedEventMessage message, Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("정산 완결 통지 수신: orderId={}", message.getOrderId());
        try {
            paymentCompletionService.confirmWalletUpdated(message.getOrderId());
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("정산 완결 통지 처리 실패 — DLQ 적재: orderId={}", message.getOrderId(), e);
            channel.basicNack(tag, false, false);
        }
    }

    @RabbitListener(queues = RabbitMqConfig.SETTLEMENT_LEDGER_COMPLETED_QUEUE)
    public void onLedgerCompleted(CompletedEventMessage message, Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("장부 완결 통지 수신: orderId={}", message.getOrderId());
        try {
            paymentCompletionService.confirmLedgerUpdated(message.getOrderId());
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("장부 완결 통지 처리 실패 — DLQ 적재: orderId={}", message.getOrderId(), e);
            channel.basicNack(tag, false, false);
        }
    }
}
