package com.bookService.core.domain.stock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 재고 차감 처리 기록 = 멱등성 테이블.
 * order_id UNIQUE 제약이 "이 주문의 재고는 이미 차감됨"을 보장하므로,
 * at-least-once 전달로 같은 payment.confirmed 메시지가 중복 도착해도
 * INSERT가 제약 위반으로 실패하면서 이중 차감을 원천 차단한다.
 * (별도 processed_message 테이블 대신 도메인 기록 자체를 멱등키로 쓰는 패턴)
 */
@Entity
@Table(name = "stock_deduction")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class StockDeduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;
}
