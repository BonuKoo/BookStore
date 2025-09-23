package com.bookService.core.domain.payment.persistent;

import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import com.bookService.core.domain.payment.dto.PendingPaymentEvent;
import com.bookService.core.domain.payment.dto.PendingPaymentOrder;
import com.bookService.core.domain.payment.dto.PendingPaymentRowDto;
import com.bookService.core.domain.payment.port.LoadPendingPaymentPort;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LoadPendingPaymentAdapter implements LoadPendingPaymentPort {

    private final SpringDataJpaPaymentEventRepository springDataJpaPaymentEventRepository;

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
}