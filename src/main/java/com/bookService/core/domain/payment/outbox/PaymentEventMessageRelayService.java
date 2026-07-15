package com.bookService.core.domain.payment.outbox;

import com.bookService.core.domain.payment.PaymentEventMessage;
import com.bookService.core.domain.payment.port.DispatchEventMessagePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Transactional Outbox 3단 중 ③ 릴레이.
 *
 * AFTER_COMMIT 즉시발행이 실패했거나(브로커 다운 등) 애초에 발행되지 못한 outbox 행을
 * 주기적으로 재발행한다. 즉시발행 경로가 있어도 릴레이가 백업으로 존재해 at-least-once
 * 전달 보장을 완성한다. 컨슈머가 멱등하므로 중복 발행은 안전하다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventMessageRelayService {

    private final PaymentOutboxService paymentOutboxService;
    private final DispatchEventMessagePort dispatchEventMessagePort;

    @Async
    @Scheduled(fixedDelay = 1, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void relay() {
        List<PaymentEventMessage> pending = paymentOutboxService.findPendingMessages();
        if (pending.isEmpty()) {
            return;
        }
        log.info("Outbox 릴레이 시작: 미확정 메시지 {}건", pending.size());

        for (PaymentEventMessage message : pending) {
            String orderId = String.valueOf(message.getPayload().get("orderId"));
            String type = message.getMessageType().name();
            try {
                dispatchEventMessagePort.dispatch(message);
                paymentOutboxService.markAsSent(orderId, type);
                log.info("Outbox 릴레이 재발행 성공. orderId={}, type={}", orderId, type);
            } catch (Exception e) {
                paymentOutboxService.markAsFailure(orderId, type);
                log.error("Outbox 릴레이 재발행 실패. orderId={}, type={}", orderId, type, e);
            }
        }
    }
}
