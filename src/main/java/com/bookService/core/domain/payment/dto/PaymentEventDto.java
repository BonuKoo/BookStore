package com.bookService.core.domain.payment.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class PaymentEventDto {

    private Long id;
    private String orderId;
    private String orderName;
    private Long buyerId;
    private boolean isPaymentDone;
    private List<PaymentOrderDto> paymentOrders;

    public PaymentEventDto(Long id, String orderId, String orderName, Long buyerId, boolean isPaymentDone) {
        this.id = id;
        this.orderId = orderId;
        this.orderName = orderName;
        this.buyerId = buyerId;
        this.isPaymentDone = isPaymentDone;
    }

    @QueryProjection
    public PaymentEventDto(Long id, String orderId, String orderName, Long buyerId, boolean isPaymentDone, List<PaymentOrderDto> paymentOrders) {
        this.id = id;
        this.orderId = orderId;
        this.orderName = orderName;
        this.buyerId = buyerId;
        this.isPaymentDone = isPaymentDone;
        this.paymentOrders = paymentOrders;
    }

    public void confirmWalletUpdate() {
        paymentOrders.forEach(PaymentOrderDto::confirmWalletUpdate);
    }

    public void confirmLedgerUpdate() {
        paymentOrders.forEach(PaymentOrderDto::confirmLedgerUpdate);
    }

    public boolean isLedgerUpdateDone() {
        return paymentOrders.stream().allMatch(PaymentOrderDto::isLedgerUpdated);
    }

    public void completeIfDone() {
        if (allPaymentOrdersDone()) {
            isPaymentDone = true;
        }
    }

    public boolean isWalletUpdateDone() {
        return paymentOrders.stream().allMatch(PaymentOrderDto::isWalletUpdated);
    }

    private boolean allPaymentOrdersDone() {
        return paymentOrders.stream().allMatch(order -> order.isWalletUpdated() && order.isLedgerUpdated());
    }
}

