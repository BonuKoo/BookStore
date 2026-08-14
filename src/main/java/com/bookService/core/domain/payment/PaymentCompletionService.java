package com.bookService.core.domain.payment;

import com.bookService.core.domain.payment.dto.PaymentEventDto;
import com.bookService.core.domain.payment.port.CompletePaymentPort;
import com.bookService.core.domain.payment.port.LoadPaymentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

/**
 * M4: settlement-worker(정산)/ledger-worker(장부)의 완결 통지를 받아 원본에서
 * 빈 클래스였던 결제 완결 흐름을 마무리한다. wallet/ledger 각각의 플래그를 갱신하고,
 * 두 플래그가 모두 참이면 결제 자체를 완결 처리한다.
 *
 * 멱등성: confirmWalletUpdate/confirmLedgerUpdate는 boolean 플래그를 true로 세팅할
 * 뿐이라 같은 통지가 중복 전달돼도 자연히 멱등하다 — 별도 가드가 필요 없다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCompletionService {

    private final LoadPaymentPort loadPaymentPort;
    private final CompletePaymentPort completePaymentPort;

    @Transactional
    public void confirmWalletUpdated(String orderId) {
        apply(orderId, PaymentEventDto::confirmWalletUpdate, completePaymentPort::handleWalletUpdate);
    }

    @Transactional
    public void confirmLedgerUpdated(String orderId) {
        apply(orderId, PaymentEventDto::confirmLedgerUpdate, completePaymentPort::handleLedgerUpdate);
    }

    private void apply(String orderId, Consumer<PaymentEventDto> confirmUpdate, Consumer<PaymentEventDto> persistFlag) {
        PaymentEventDto dto = loadPaymentPort.getPaymentEventAndOrders(orderId);
        if (dto == null) {
            log.warn("완결 통지를 받았으나 매칭되는 결제 이벤트가 없음 — orderId={}", orderId);
            return;
        }

        confirmUpdate.accept(dto);
        persistFlag.accept(dto);

        // dto.completeIfDone()/isPaymentDone()은 이 스레드가 조회 시점에 들고 있던
        // in-memory 스냅샷으로 판정하므로, wallet/ledger 통지가 병렬로 도착하면
        // 서로 상대방의 커밋을 보지 못해 아무도 완결 처리를 하지 않는 레이스가 있었다.
        // tryMarkPaymentDone은 DB의 현재 상태를 조건으로 원자적으로 재확인하므로
        // 정확히 한 번만 true를 반환한다.
        if (completePaymentPort.tryMarkPaymentDone(orderId)) {
            log.info("결제 완결 처리됨: orderId={}", orderId);
        }
    }
}
