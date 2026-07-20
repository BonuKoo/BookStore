package com.bookService.core.infra.loadtest;

import com.bookService.core.domain.payment.dto.PaymentConfirmCommand;
import com.bookService.core.domain.payment.dto.PaymentExecutionResult;
import com.bookService.core.domain.payment.dto.PaymentExtraDetails;
import com.bookService.core.domain.payment.enumtype.PSPConfirmationStatus;
import com.bookService.core.domain.payment.enumtype.PaymentMethod;
import com.bookService.core.domain.payment.enumtype.PaymentType;
import com.bookService.core.domain.payment.port.PaymentExecutorPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * k6 부하 테스트 전용 PSP 스텁 (Phase 6).
 *
 * Toss 실결제는 부하 테스트에서 대량 호출할 수 없으므로, loadtest 프로파일에서만
 * 이 스텁이 {@link PaymentExecutorPort}의 @Primary 구현체로 올라와 외부 HTTP 없이
 * 즉시 승인 결과를 돌려준다. confirm 이후의 전 경로(상태 전이 → Outbox →
 * AFTER_COMMIT 발행 → 브로커 → 워커 3종 → 완결 수신)는 실제 코드가 그대로 돈다.
 *
 * 기본 프로파일에서는 빈이 생성되지 않으므로 운영 동작에 영향 없다.
 */
@Slf4j
@Service
@Primary
@Profile("loadtest")
public class LoadTestPaymentExecutorStub implements PaymentExecutorPort {

    @Override
    public PaymentExecutionResult execute(PaymentConfirmCommand command) {
        log.debug("[loadtest] PSP 승인 스텁 통과: orderId={}", command.getOrderId());
        return new PaymentExecutionResult(
                command.getPaymentKey(),
                command.getOrderId(),
                new PaymentExtraDetails(
                        PaymentType.NORMAL,
                        PaymentMethod.EASY_PAY,
                        LocalDateTime.now(),
                        "loadtest-order",
                        PSPConfirmationStatus.DONE,
                        command.getAmount(),
                        "{\"loadtest\":true}"
                ),
                null,
                true, false, false, false
        );
    }
}
