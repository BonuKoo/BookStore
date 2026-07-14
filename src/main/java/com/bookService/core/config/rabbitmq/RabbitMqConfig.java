package com.bookService.core.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String PAYMENT_EXCHANGE = "payment.exchange";

    public static final String PAYMENT_CONFIRMED_QUEUE = "payment.confirmed.queue";
    public static final String PAYMENT_CONFIRMED_ROUTING_KEY = "payment.confirmed";

    public static final String PAYMENT_FAILED_QUEUE = "payment.failed.queue";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";

    // 재고 차감 전용 큐. 알림 큐(payment.confirmed.queue)와 같은 routing key에 바인딩되어
    // 하나의 payment.confirmed 이벤트를 두 컨슈머(PC3 알림 워커 / core-spa 재고 차감)가
    // 각자 독립적으로 소비한다 — topic exchange 팬아웃.
    public static final String STOCK_DEDUCTION_QUEUE = "stock.deduction.queue";

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue paymentConfirmedQueue() {
        return QueueBuilder.durable(PAYMENT_CONFIRMED_QUEUE).build();
    }

    @Bean
    public Queue paymentFailedQueue() {
        return QueueBuilder.durable(PAYMENT_FAILED_QUEUE).build();
    }

    @Bean
    public Binding paymentConfirmedBinding(Queue paymentConfirmedQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentConfirmedQueue).to(paymentExchange).with(PAYMENT_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    public Binding paymentFailedBinding(Queue paymentFailedQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentFailedQueue).to(paymentExchange).with(PAYMENT_FAILED_ROUTING_KEY);
    }

    @Bean
    public Queue stockDeductionQueue() {
        return QueueBuilder.durable(STOCK_DEDUCTION_QUEUE).build();
    }

    @Bean
    public Binding stockDeductionBinding(Queue stockDeductionQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(stockDeductionQueue).to(paymentExchange).with(PAYMENT_CONFIRMED_ROUTING_KEY);
    }

    // Spring Boot가 자동구성한 ObjectMapper(JavaTimeModule 등록, 날짜를 배열이 아닌
    // ISO-8601 문자열로 직렬화)를 그대로 재사용한다. 기본 생성자로 만들면 별도의
    // ObjectMapper가 생성되어 LocalDateTime이 [2026,7,14,...] 배열로 직렬화되는데,
    // 컨슈머(notification-worker)는 다른 코드베이스라 이 포맷을 파싱하기 번거롭다.
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
