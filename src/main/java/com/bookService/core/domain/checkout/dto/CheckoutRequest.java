package com.bookService.core.domain.checkout.dto;

import com.bookService.core.domain.cartitem.CartItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class CheckoutRequest {

    private List<Long> cartItemIds;  //  주문 목록
    private String seed;            // 시간

    public CheckoutRequest(List<Long> cartItemIds, String seed) {
        this.cartItemIds = cartItemIds;
        this.seed = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString();
    }
}

