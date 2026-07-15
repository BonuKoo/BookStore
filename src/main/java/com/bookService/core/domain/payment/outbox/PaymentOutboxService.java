package com.bookService.core.domain.payment.outbox;

import com.bookService.core.domain.payment.PaymentEventMessage;
import com.bookService.core.domain.payment.enumtype.PaymentEventMessageType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Transactional Outbox 3단 중 ①저장과 마킹/조회를 담당한다.
 *
 * - {@link #insertOutbox}: 결제 상태 전이 트랜잭션 <b>내부</b>에서 호출되어(전파 REQUIRED)
 *   발행할 메시지를 INIT 상태로 같은 트랜잭션에 커밋한다.
 * - {@link #markAsSent}/{@link #markAsFailure}: AFTER_COMMIT 리스너·릴레이에서 호출되며
 *   원본 트랜잭션이 이미 종료된 시점이라 각자 새 트랜잭션(REQUIRES_NEW)에서 상태를 갱신한다.
 * - {@link #findPendingMessages}: 릴레이가 재발행할 미확정 행을 메시지로 복원한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOutboxService {

    /**
     * 릴레이가 방금 커밋된(AFTER_COMMIT 즉시발행이 진행 중일 수 있는) 행을 중복 발행하지
     * 않도록 두는 유예 시간(초). 원본은 1분이었으나, 즉시발행이 실패해도 빠르게 따라잡도록 축소.
     */
    private static final long RELAY_GRACE_SECONDS = 10;

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /** ① 발행할 메시지를 outbox에 INIT 상태로 저장한다. 호출자의 트랜잭션에 합류한다. */
    @Transactional(propagation = Propagation.REQUIRED)
    public void insertOutbox(PaymentEventMessage message) {
        Outbox outbox = Outbox.builder()
                .idempotencyKey(orderIdOf(message))
                .type(message.getMessageType().name())
                .status(OutboxStatus.INIT)
                .payload(toJson(message.getPayload()))
                .metadata(toJson(message.getMetadata()))
                .build();
        outboxRepository.save(outbox);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsSent(String orderId, String type) {
        outboxRepository.updateStatus(orderId, type, OutboxStatus.SUCCESS);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsFailure(String orderId, String type) {
        outboxRepository.updateStatus(orderId, type, OutboxStatus.FAILURE);
    }

    /** ③ 릴레이 대상(INIT/FAILURE, 유예시간 경과)을 발행 가능한 메시지로 복원한다. */
    @Transactional(readOnly = true)
    public List<PaymentEventMessage> findPendingMessages() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(RELAY_GRACE_SECONDS);
        return outboxRepository
                .findPending(List.of(OutboxStatus.INIT, OutboxStatus.FAILURE), threshold)
                .stream()
                .map(this::toMessage)
                .toList();
    }

    private PaymentEventMessage toMessage(Outbox outbox) {
        return new PaymentEventMessage(
                PaymentEventMessageType.valueOf(outbox.getType()),
                fromJson(outbox.getPayload()),
                fromJson(outbox.getMetadata()));
    }

    private String orderIdOf(PaymentEventMessage message) {
        Object orderId = message.getPayload().get("orderId");
        if (orderId == null) {
            throw new IllegalArgumentException("Outbox 저장 실패: payload에 orderId가 없습니다.");
        }
        return orderId.toString();
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox payload JSON 직렬화 실패", e);
        }
    }

    private Map<String, Object> fromJson(String json) {
        if (json == null) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Outbox payload JSON 역직렬화 실패", e);
        }
    }
}
