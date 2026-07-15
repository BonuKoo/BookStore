package com.bookService.core.domain.payment;

import com.bookService.core.domain.payment.outbox.PaymentOutboxService;
import com.bookService.core.domain.payment.port.DispatchEventMessagePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 결제 상태 전이 트랜잭션이 커밋된 이후에만 메시지를 브로커로 전송한다(즉시발행 경로).
 * 트랜잭션 내부에서 바로 전송하면 롤백 시에도 메시지가 나가버리는 문제가 생기므로,
 * PaymentStatusUpdateRepository는 여기로 도메인 이벤트(PaymentEventMessage)만 던지고
 * 실제 dispatch는 AFTER_COMMIT 시점에 이 리스너가 수행한다.
 *
 * <p>Transactional Outbox와의 관계: 발행 대상은 이미 같은 트랜잭션에서 outbox에 INIT으로
 * 저장돼 있다. 여기서 발행에 성공하면 해당 행을 SUCCESS로 마킹해 릴레이가 다시 집어가지
 * 않게 하고, 실패하면 FAILURE로 마킹해 릴레이의 재발행 대상으로 남긴다. 발행 실패를
 * 여기서 되던지지 않는 이유는 이미 커밋된 트랜잭션을 되돌릴 수 없고, 재전송은 릴레이가
 * 책임지기 때문이다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventMessagePublishListener {

    private final DispatchEventMessagePort dispatchEventMessagePort;
    private final PaymentOutboxService paymentOutboxService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentEventMessage(PaymentEventMessage message) {
        String orderId = String.valueOf(message.getPayload().get("orderId"));
        String type = message.getMessageType().name();
        try {
            dispatchEventMessagePort.dispatch(message);
            paymentOutboxService.markAsSent(orderId, type);
        } catch (Exception e) {
            paymentOutboxService.markAsFailure(orderId, type);
            log.error("결제 이벤트 즉시발행 실패 — 릴레이가 재발행한다. orderId={}, type={}", orderId, type, e);
        }
    }
}
