package com.bookService.core.domain.stock;

import com.bookService.core.config.rabbitmq.RabbitMqConfig;
import com.bookService.core.domain.payment.PaymentEventMessage;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * payment.confirmed를 알림 큐와 별개의 전용 큐(stock.deduction.queue)로 구독한다.
 * 같은 이벤트를 여러 컨슈머가 독립 소비하는 팬아웃 — 프로듀서는 변경 없음.
 *
 * 실패 정책(Phase 4):
 *  - 중복 전달 → 멱등 가드가 걸러냄. 정상 상황이므로 info 로그 후 ack.
 *  - 재고 부족 → 재시도해도 해소되지 않는 영구 실패이므로 nack(requeue=false)로
 *    stock.deduction.queue.dlq에 적재해 수동 개입 대상으로 남긴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockDeductionListener {

    private final StockDeductionService stockDeductionService;

    @SuppressWarnings("unchecked")
    @RabbitListener(queues = RabbitMqConfig.STOCK_DEDUCTION_QUEUE)
    public void onPaymentConfirmed(PaymentEventMessage message, Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        String orderId = String.valueOf(message.getPayload().get("orderId"));

        // 재전달의 흔한 경우를 예외 없이 빠르게 걸러내는 사전 확인.
        // (동시 중복은 이 확인을 통과할 수 있지만 UNIQUE 제약이 최종 방어선)
        if (stockDeductionService.alreadyProcessed(orderId)) {
            log.info("이미 재고 차감된 주문 — 중복 전달 skip: orderId={}", orderId);
            channel.basicAck(tag, false);
            return;
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) message.getPayload().get("items");
        if (items == null || items.isEmpty()) {
            log.warn("items가 비어 있는 payment.confirmed 수신 — 차감할 것 없음: orderId={}", orderId);
            channel.basicAck(tag, false);
            return;
        }

        try {
            stockDeductionService.deduct(orderId, items);
            channel.basicAck(tag, false);
        } catch (DataIntegrityViolationException e) {
            log.info("이미 재고 차감된 주문 — 동시 중복 전달 skip: orderId={}", orderId);
            channel.basicAck(tag, false);
        } catch (InsufficientStockException e) {
            log.error("재고 부족으로 차감 실패 — DLQ 적재 후 수동 개입 필요: {}", e.getMessage());
            channel.basicNack(tag, false, false);
        }
    }
}
