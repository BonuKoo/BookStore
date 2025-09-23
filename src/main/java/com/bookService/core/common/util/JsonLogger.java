package com.bookService.core.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
public class JsonLogger {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void info(String context, String message, Object data) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "context", context,
                    "message", message,
                    "data", data
            ));
            log.info(json);
        } catch (JsonProcessingException e) {
            log.info("context={}, message={}, data={}", context, message, data);
        }
    }

    public void error(String context, String message, Throwable throwable) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "context", context,
                    "message", message,
                    "exception", throwable.toString(),
                    "stacktrace", Arrays.toString(throwable.getStackTrace())
            ));
            log.error(json);
        } catch (JsonProcessingException e) {
            log.error("context={}, message={}, error={}", context, message, throwable.getMessage(), throwable);
        }
    }

}
