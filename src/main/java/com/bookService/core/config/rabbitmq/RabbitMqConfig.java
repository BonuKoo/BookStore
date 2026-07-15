package com.bookService.core.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.AcknowledgeMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    // M4: settlement-worker/ledger-worker의 완결 통지 수신용. 두 워커가 먼저 선언해
    // 두었을 수 있지만(RabbitAdmin 선언은 멱등적), core-spa도 소비자로서 동일하게
    // 선언해 워커보다 먼저 뜨는 경우에도 큐를 찾을 수 있게 한다.
    public static final String SETTLEMENT_WALLET_COMPLETED_QUEUE = "settlement.wallet.completed.queue";
    public static final String SETTLEMENT_WALLET_COMPLETED_ROUTING_KEY = "settlement.wallet.completed";

    public static final String SETTLEMENT_LEDGER_COMPLETED_QUEUE = "settlement.ledger.completed.queue";
    public static final String SETTLEMENT_LEDGER_COMPLETED_ROUTING_KEY = "settlement.ledger.completed";

    // Phase 4: 자동 dead-lettering. 큐마다 x-dead-letter-exchange로 이 direct exchange를
    // 지정하고, x-dead-letter-routing-key로 "<큐 이름>.dlq"를 지정한다. 이 큐를 함께
    // 선언하는 다른 프로젝트(notification-worker, settlement-worker, ledger-worker)의
    // 선언과 인자가 한 글자도 다르면 안 된다 — RabbitMQ가 기존 큐와 다른 인자로 재선언을
    // 시도하면 406 PRECONDITION_FAILED로 거부한다.
    public static final String DLX_EXCHANGE = "payment.dlx";

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue paymentConfirmedQueue() {
        return QueueBuilder.durable(PAYMENT_CONFIRMED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", PAYMENT_CONFIRMED_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue paymentConfirmedDlq() {
        return QueueBuilder.durable(PAYMENT_CONFIRMED_QUEUE + ".dlq").build();
    }

    @Bean
    public Binding paymentConfirmedDlqBinding(Queue paymentConfirmedDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(paymentConfirmedDlq).to(dlxExchange).with(PAYMENT_CONFIRMED_QUEUE + ".dlq");
    }

    @Bean
    public Queue paymentFailedQueue() {
        return QueueBuilder.durable(PAYMENT_FAILED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", PAYMENT_FAILED_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue paymentFailedDlq() {
        return QueueBuilder.durable(PAYMENT_FAILED_QUEUE + ".dlq").build();
    }

    @Bean
    public Binding paymentFailedDlqBinding(Queue paymentFailedDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(paymentFailedDlq).to(dlxExchange).with(PAYMENT_FAILED_QUEUE + ".dlq");
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
        return QueueBuilder.durable(STOCK_DEDUCTION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", STOCK_DEDUCTION_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue stockDeductionDlq() {
        return QueueBuilder.durable(STOCK_DEDUCTION_QUEUE + ".dlq").build();
    }

    @Bean
    public Binding stockDeductionDlqBinding(Queue stockDeductionDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(stockDeductionDlq).to(dlxExchange).with(STOCK_DEDUCTION_QUEUE + ".dlq");
    }

    @Bean
    public Binding stockDeductionBinding(Queue stockDeductionQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(stockDeductionQueue).to(paymentExchange).with(PAYMENT_CONFIRMED_ROUTING_KEY);
    }

    // settlement-worker도 이 큐를 선언한다(M4) — 인자를 반드시 동일하게 유지.
    @Bean
    public Queue settlementWalletCompletedQueue() {
        return QueueBuilder.durable(SETTLEMENT_WALLET_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SETTLEMENT_WALLET_COMPLETED_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue settlementWalletCompletedDlq() {
        return QueueBuilder.durable(SETTLEMENT_WALLET_COMPLETED_QUEUE + ".dlq").build();
    }

    @Bean
    public Binding settlementWalletCompletedDlqBinding(Queue settlementWalletCompletedDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(settlementWalletCompletedDlq).to(dlxExchange)
                .with(SETTLEMENT_WALLET_COMPLETED_QUEUE + ".dlq");
    }

    @Bean
    public Binding settlementWalletCompletedBinding(Queue settlementWalletCompletedQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(settlementWalletCompletedQueue).to(paymentExchange)
                .with(SETTLEMENT_WALLET_COMPLETED_ROUTING_KEY);
    }

    // ledger-worker도 이 큐를 선언한다(M4) — 인자를 반드시 동일하게 유지.
    @Bean
    public Queue settlementLedgerCompletedQueue() {
        return QueueBuilder.durable(SETTLEMENT_LEDGER_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SETTLEMENT_LEDGER_COMPLETED_QUEUE + ".dlq")
                .build();
    }

    @Bean
    public Queue settlementLedgerCompletedDlq() {
        return QueueBuilder.durable(SETTLEMENT_LEDGER_COMPLETED_QUEUE + ".dlq").build();
    }

    @Bean
    public Binding settlementLedgerCompletedDlqBinding(Queue settlementLedgerCompletedDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(settlementLedgerCompletedDlq).to(dlxExchange)
                .with(SETTLEMENT_LEDGER_COMPLETED_QUEUE + ".dlq");
    }

    @Bean
    public Binding settlementLedgerCompletedBinding(Queue settlementLedgerCompletedQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(settlementLedgerCompletedQueue).to(paymentExchange)
                .with(SETTLEMENT_LEDGER_COMPLETED_ROUTING_KEY);
    }

    // Phase 4: 컨슈머가 manual ack로 직접 ack/nack(requeue=false)해야 자동 dead-lettering이
    // 발동한다. 모든 @RabbitListener가 이 팩토리(기본 빈 이름 rabbitListenerContainerFactory)를 쓴다.
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }

    // Spring Boot가 자동구성한 ObjectMapper(JavaTimeModule 등록, 날짜를 배열이 아닌
    // ISO-8601 문자열로 직렬화)를 그대로 재사용한다. 기본 생성자로 만들면 별도의
    // ObjectMapper가 생성되어 LocalDateTime이 [2026,7,14,...] 배열로 직렬화되는데,
    // 컨슈머(notification-worker)는 다른 코드베이스라 이 포맷을 파싱하기 번거롭다.
    //
    // M4: settlement-worker/ledger-worker가 보내는 완결 통지 메시지의 __TypeId__
    // 헤더는 그쪽 코드베이스의 클래스명(예: com.bookService.settlement.dto.WalletCompletedMessage)
    // 이라 이 프로젝트엔 없는 클래스다. TypePrecedence를 INFERRED로 두면 그 헤더를
    // 무시하고 @RabbitListener 메서드의 파라미터 타입으로만 역직렬화한다 — 이 앱이
    // 자기 자신의 PaymentEventMessage를 소비하는 기존 리스너들에도 안전하게 적용된다.
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    // Phase 4: publisher confirms(브로커가 메시지를 실제로 받았는지)와 returns(어떤 큐에도
    // 라우팅되지 못한 메시지)를 콜백으로 관측한다. spring.rabbitmq.publisher-confirm-type=
    // correlated / publisher-returns=true가 켜져 있어야 두 콜백이 실제로 호출된다.
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.warn("발행 confirm 실패(broker nack) — 메시지가 브로커에 반영되지 않았을 수 있음. cause={}", cause);
            }
        });
        rabbitTemplate.setReturnsCallback(returned -> log.warn(
                "발행 메시지가 어떤 큐에도 라우팅되지 못해 반환됨(unroutable). exchange={}, routingKey={}, replyText={}",
                returned.getExchange(), returned.getRoutingKey(), returned.getReplyText()));
        return rabbitTemplate;
    }
}
