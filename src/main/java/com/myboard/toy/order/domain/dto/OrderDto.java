package com.myboard.toy.order.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderDto {

    private Long orderId;
    private int totalAmount;

    @Builder
    public OrderDto(Long orderId,int totalAmount) {
        this.orderId = orderId;
        this.totalAmount = totalAmount;
    }
}
