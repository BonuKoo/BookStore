package com.bookService.core.domain.checkout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutCommand {

    private Long cartId;
    private Long buyerId;
    private List<Long> cartItemIds;
    private String idempotencyKey;

}
