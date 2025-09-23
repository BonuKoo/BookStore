package com.bookService.core.domain.payment.dto;


import com.bookService.core.domain.payment.enumtype.PSPConfirmationStatus;
import com.bookService.core.domain.payment.enumtype.PaymentMethod;
import com.bookService.core.domain.payment.enumtype.PaymentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class PaymentExtraDetails {

    private final PaymentType type;
    private final PaymentMethod method;
    private final LocalDateTime approvedAt;
    private final String orderName;
    private final PSPConfirmationStatus pspConfirmationStatus;
    private final long totalAmount;
    private final String pspRawData;

    public PaymentExtraDetails(PaymentType type, PaymentMethod method, LocalDateTime approvedAt,
                               String orderName, PSPConfirmationStatus pspConfirmationStatus,
                               long totalAmount, String pspRawData) {
        this.type = type;
        this.method = method;
        this.approvedAt = approvedAt;
        this.orderName = orderName;
        this.pspConfirmationStatus = pspConfirmationStatus;
        this.totalAmount = totalAmount;
        this.pspRawData = pspRawData;
    }

}
