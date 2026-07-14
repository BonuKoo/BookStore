package com.bookService.core.domain.payment.adapter;

import com.bookService.core.config.rabbitmq.RabbitMqConfig;
import com.bookService.core.domain.payment.PaymentEventMessage;
import com.bookService.core.domain.payment.enumtype.PaymentEventMessageType;
import com.bookService.core.domain.payment.port.DispatchEventMessagePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitDispatchEventMessageAdapter implements DispatchEventMessagePort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void dispatch(PaymentEventMessage message) {
        String routingKey = routingKeyOf(message.getMessageType());
        log.info("Dispatching payment event message. type={}, routingKey={}, orderId={}",
                message.getMessageType(), routingKey, message.getPayload().get("orderId"));
        rabbitTemplate.convertAndSend(RabbitMqConfig.PAYMENT_EXCHANGE, routingKey, message);
    }

    private String routingKeyOf(PaymentEventMessageType messageType) {
        return switch (messageType) {
            case PAYMENT_CONFIRMATION_SUCCESS -> RabbitMqConfig.PAYMENT_CONFIRMED_ROUTING_KEY;
            case PAYMENT_CONFIRMATION_FAILURE -> RabbitMqConfig.PAYMENT_FAILED_ROUTING_KEY;
        };
    }
}
