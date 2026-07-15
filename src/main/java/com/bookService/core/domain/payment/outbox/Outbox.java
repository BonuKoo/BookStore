package com.bookService.core.domain.payment.outbox;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Transactional Outbox 행.
 *
 * 결제 상태 전이 트랜잭션과 <b>같은 트랜잭션</b>에서 이 행을 INIT 상태로 저장한다.
 * "발행해야 할 사실"을 비즈니스 데이터와 원자적으로 함께 커밋하므로, 커밋 직후
 * 브로커 발행이 실패(브로커 다운 등)해도 이 행이 남아 릴레이 스케줄러가 재발행한다.
 *
 * 원본(paymentModel)의 Kafka partitionKey는 RabbitMQ에선 불필요하여 제거했다
 * (순서 보장은 단일 큐 + 단일 컨슈머의 자연 직렬화로 대체).
 */
@Entity
@Table(
        name = "outbox",
        uniqueConstraints = @UniqueConstraint(name = "uk_outbox_idempotency_key", columnNames = "idempotency_key")
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 멱등키 = orderId. 주문당 terminal 결제 상태는 하나뿐이라 UNIQUE가 성립한다. */
    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.INIT;

    /** PaymentEventMessageType 이름 (PAYMENT_CONFIRMATION_SUCCESS / _FAILURE) */
    @Column(nullable = false, length = 40)
    private String type;

    @Column(columnDefinition = "json", nullable = false)
    private String payload;

    @Column(columnDefinition = "json")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
