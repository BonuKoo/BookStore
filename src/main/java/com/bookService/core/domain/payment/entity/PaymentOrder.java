package com.bookService.core.domain.payment.entity;

import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


//@SequenceGenerator(
//        name = "payment_order_seq_generator",
//        sequenceName = "payment_order_seq",
//        allocationSize = 500
//)
@Entity @Table(name = "payment_orders")
@AllArgsConstructor @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder @Getter
public class PaymentOrder {
    /*
    @Id
    @GeneratedValue(
            strategy = GenerationType.TABLE, // 전략을 TABLE로 변경
            generator = "payment_order_seq_generator" // 사용할 Generator 이름 정의
    )
    @TableGenerator(
            name = "payment_order_seq_generator",
            table = "hibernate_sequences", // ID를 관리할 테이블 (별도 생성 필요)
            pkColumnName = "sequence_name",
            valueColumnName = "next_val",
            initialValue = 1,
            // allocationSize를 통해 DB에 ID를 한 번에 몇 개씩 요청할지 정의합니다.
            // 이 값이 커야 Batch Insert가 효율적입니다.
            allocationSize = 50
    )*/

    // (1) Sequence Generator 정의
//    @SequenceGenerator(
//            name = "payment_order_seq_generator",
//            sequenceName = "payment_order_seq", // DB에 생성될 시퀀스 이름
//            initialValue = 1,
//            // ID를 한 번에 50개씩 미리 할당하여 Batch Insert를 활성화합니다.
//            allocationSize = 500
//    )
//    // (2) SEQUENCE 전략 사용 및 Generator 지정
//    @Id
//    @GeneratedValue(
//            strategy = GenerationType.SEQUENCE,
//            generator = "payment_order_seq_generator" // 위에서 정의한 Generator 이름
//    )

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentEvent paymentEvent;

    @Column(nullable = true)
    private Long sellerId; // 판매자 ID

    @Column(nullable = false)   // Account로 바꾸
    private String productId; // 상품 ID

    @Column(name = "order_id")  // Item으로 바꾸
    private String orderId;

    @Column(nullable = false)
    private int amount; // 결제 금액

    // 주문 수량. 재고 차감 컨슈머(payment.confirmed → stock.deduction.queue)가
    // 차감량을 알 수 있도록 주문 스냅샷에 보존한다. (금액만 있으면 수량 복원 불가)
    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    /*
        DB ON? NO?
     */
    @Column
    private int failed_count;

    @Column
    private int threshold;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** todo
     *
     */

    @Column(name = "is_ledger_updated",nullable = false)
    private boolean isLedgerUpdated; //장부 기입 여부

    @Column(name = "is_wallet_updated",nullable = false)
    private boolean isWalletUpdated; //정산 처리 여부

    // 메서드
    public boolean isLedgerUpdated() {
        return isLedgerUpdated;
    }

    public boolean isWalletUpdated() {
        return isWalletUpdated;
    }

    public void confirmWalletUpdate() {
        isWalletUpdated = true;
    }

    public void confirmLedgerUpdate() {
        isLedgerUpdated = true;
    }

    public void setPaymentEvent(PaymentEvent paymentEvent) {
        this.paymentEvent = paymentEvent;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
