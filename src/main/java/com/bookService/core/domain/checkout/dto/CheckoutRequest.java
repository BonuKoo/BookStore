package com.bookService.core.domain.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    private List<Long> cartItemIds;  //  주문 목록
    private String seed = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString();            // 시간
}

