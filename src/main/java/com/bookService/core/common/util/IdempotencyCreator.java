package com.bookService.core.common.util;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

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

}
