package com.bookService.core.common.util;

import com.bookService.core.domain.checkout.dto.CheckoutRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class IdempotencyCreator {

    private IdempotencyCreator() {
        // 인스턴스화 방지
    }

    public static String create(Object data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null");
        }
        return UUID.nameUUIDFromBytes(data.toString().getBytes(StandardCharsets.UTF_8)).toString();
    }

    public static String create(CheckoutRequest request, String userId) {

        if (request == null || userId == null) {
            throw new IllegalArgumentException("Request and userId must not be null");
        }
        List<Long> itemIds = request.getCartItemIds();

        // 🚨 핵심: 순서와 상관없이 동일한 키를 만들기 위해 정렬
        Collections.sort(itemIds);

        // userId와 정렬된 itemIds를 조합한 문자열 생성
        String uniqueData = userId + ":" + itemIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return UUID.nameUUIDFromBytes(uniqueData.getBytes(StandardCharsets.UTF_8)).toString();
    }

}
