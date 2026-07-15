package com.bookService.core.domain.payment.persistent.repository.jpa;

import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import com.bookService.core.domain.payment.dto.PaymentEventDto;
import com.bookService.core.domain.payment.dto.PendingPaymentEvent;
import com.bookService.core.domain.payment.dto.PendingPaymentOrder;
import com.bookService.core.domain.payment.dto.PendingPaymentRowDto;
import com.bookService.core.domain.payment.entity.PaymentEvent;
import com.bookService.core.domain.payment.persistent.repository.PaymentEventRepository;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentEventRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaPaymentEventRepository implements PaymentEventRepository {

    private final SpringDataJpaPaymentEventRepository springDataJpaPaymentEventRepository;

    @Override
    public void save(PaymentEvent paymentEvent) {
        springDataJpaPaymentEventRepository.save(paymentEvent);
    }


    public Optional<PaymentEvent> findByOrderId(String orderId){
        return springDataJpaPaymentEventRepository.findByOrderId(orderId);
    }

    @Override
    public List<PendingPaymentEvent> getPendingPayments() {
        List<PendingPaymentRowDto> rows = springDataJpaPaymentEventRepository.findPendingPaymentRows(LocalDateTime.now());

        return rows.stream()
                .collect(Collectors.groupingBy(PendingPaymentRowDto::getPaymentEventId))
                .entrySet().stream()
                .map(entry -> {
                    List<PendingPaymentRowDto> group = entry.getValue();
                    PendingPaymentRowDto first = group.get(0);

                    List<PendingPaymentOrder> orders = group.stream()
                            .map(r -> new PendingPaymentOrder(
                                    r.getPaymentOrderId(),
                                    PaymentStatus.get(r.getPaymentOrderStatus()),
                                    r.getAmount().longValue(),
                                    r.getFailedCount(),
                                    r.getThreshold()
                            ))
                            .collect(Collectors.toList());

                    return new PendingPaymentEvent(
                            first.getPaymentEventId(),
                            first.getPaymentKey(),
                            first.getOrderId(),
                            orders
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PendingPaymentRowDto> findPendingPaymentRows(LocalDateTime now) {
        return springDataJpaPaymentEventRepository.findPendingPaymentRows(now);
    }

    @Override
    public PaymentEvent getPayment(String orderName) {
        return springDataJpaPaymentEventRepository.findByOrderName(orderName).orElseThrow();
    }

    @Override
    public PaymentEventDto getPaymentEventAndOrders(String orderId){
        return springDataJpaPaymentEventRepository.getPaymentEventAndOrders(orderId);
    }

    @Override
    @Transactional
    public void complete(PaymentEventDto paymentEventDto) {
        if (paymentEventDto.isPaymentDone()) {
            // 모든 업데이트 및 완료 처리 포함
            springDataJpaPaymentEventRepository.handlePaymentCompletion(paymentEventDto);
        } else {
            throw new IllegalStateException("Incorrect state for PaymentEvent id: " + paymentEventDto.getId());
        }
    }

    // M4: wallet/ledger 완결 통지를 받을 때마다 각각의 플래그만 별도로 반영한다.
    // 완료 여부(complete())와는 별개 — 호출측(PaymentCompletionService)이
    // completeIfDone() 판단 후 필요할 때만 complete()를 호출한다.
    @Override
    @Transactional
    public void handleWalletUpdate(PaymentEventDto paymentEventDto) {
        springDataJpaPaymentEventRepository.handleWalletUpdate(paymentEventDto);
    }

    @Override
    @Transactional
    public void handleLedgerUpdate(PaymentEventDto paymentEventDto) {
        springDataJpaPaymentEventRepository.handleLedgerUpdate(paymentEventDto);
    }
}
