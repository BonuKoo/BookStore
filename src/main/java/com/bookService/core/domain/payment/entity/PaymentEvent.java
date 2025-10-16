package com.bookService.core.domain.payment.entity;

import com.bookService.core.domain.login.entity.AccountEntity;
import com.bookService.core.domain.payment.enumtype.PaymentMethod;
import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import com.bookService.core.domain.payment.enumtype.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/*
@SequenceGenerator(
        name = "payment_event_seq_generator",
        sequenceName = "payment_event_seq", // DB에 생성될 시퀀스명
        allocationSize = 500                 // 미리 50개 ID를 가져와 batch insert 활성화
)
*/
@Entity @Table(name = "payment_event")
@AllArgsConstructor @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data @Builder
public class PaymentEvent {

    @Id @Column(name = "payment_event_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_event_seq_generator")
    private Long id;

    @Version
    @Column(name = "version")
    private Long version; // Lock

    @Column(name = "buyer_id", nullable = true)
    private Long buyerId; // 결제자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer", nullable = false) // AccountEntity의 PK를 참조하는 외래 키 컬럼
    private AccountEntity accountEntity;

    @Column(name = "is_payment_done", nullable = false)
    private boolean isPaymentDone;

    @Column(nullable = true)
    private String paymentKey; // 외부 결제 키

    @Column(name = "order_id",unique = true)
    private String orderId;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Column(name = "order_name", nullable = false)
    private String orderName;

    @Enumerated(EnumType.STRING)
    @Column(length = 255)
    private PaymentMethod method;

    @Lob
    @Column(name = "psp_raw_data", columnDefinition = "TEXT")
    private String pspRawData;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 결제 승인된 시각
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @OneToMany(mappedBy = "paymentEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentOrder> paymentOrders;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus; // (PENDING, SUCCESS, FAILURE)

    public long totalAmount(){
        return paymentOrders.stream()
                .mapToLong(PaymentOrder::getAmount)
                .sum();
    }

    public boolean isSuccess() {
        return paymentStatus == PaymentStatus.SUCCESS;
    }

    public boolean isFailure() {
        return paymentOrders.stream().allMatch(order -> order.getPaymentStatus() == PaymentStatus.FAILURE);
    }

    public boolean isUnknown() {
        return paymentOrders.stream().allMatch(order -> order.getPaymentStatus() == PaymentStatus.UNKNOWN);
    }
    public void updatePaymentStatus(PaymentStatus status) {
        this.paymentStatus = status;
    }

    public void setPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }

    public void setPaymentOrders(List<PaymentOrder> paymentOrders){
        this.paymentOrders = paymentOrders;
    }


    public void confirmWalletUpdate() {
        paymentOrders.forEach(PaymentOrder::confirmWalletUpdate);
    }

    public void confirmLedgerUpdate() {
        paymentOrders.forEach(PaymentOrder::confirmLedgerUpdate);
    }

    public boolean isLedgerUpdateDone() {
        return paymentOrders.stream().allMatch(PaymentOrder::isLedgerUpdated);
    }

    public boolean isWalletUpdateDone() {
        return paymentOrders.stream().allMatch(PaymentOrder::isWalletUpdated);
    }

    public void completeIfDone() {
        if (allPaymentOrdersDone()) {
            isPaymentDone = true;
        }
    }

    private boolean allPaymentOrdersDone() {
        return paymentOrders.stream().allMatch(order -> order.isWalletUpdated() && order.isLedgerUpdated());
    }

}
