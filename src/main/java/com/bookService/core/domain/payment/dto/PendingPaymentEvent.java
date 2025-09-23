package com.bookService.core.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingPaymentEvent {

    private Long paymentEventId;
    private String paymentKey;
    private String orderId;
    private List<PendingPaymentOrder> pendingPaymentOrders;

    public Long totalAmount(){
        return pendingPaymentOrders.stream()
                .mapToLong(PendingPaymentOrder::getAmount)
                .sum();
    }

}
