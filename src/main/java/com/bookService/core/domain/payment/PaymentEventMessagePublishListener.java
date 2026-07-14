package com.bookService.core.domain.payment;

import com.bookService.core.domain.payment.port.DispatchEventMessagePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 결제 상태 전이 트랜잭션이 커밋된 이후에만 메시지를 브로커로 전송한다.
 * 트랜잭션 내부에서 바로 전송하면 롤백 시에도 메시지가 나가버리는 문제가 생기므로,
 * PaymentStatusUpdateRepository는 여기로 도메인 이벤트(PaymentEventMessage)만 던지고
 * 실제 dispatch는 AFTER_COMMIT 시점에 이 리스너가 수행한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventMessagePublishListener {

    private final DispatchEventMessagePort dispatchEventMessagePort;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentEventMessage(PaymentEventMessage message) {
        dispatchEventMessagePort.dispatch(message);
    }
}
