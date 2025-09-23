package com.bookService.core.test.cart.mysql.service;

import com.bookService.core.domain.payment.entity.PaymentEvent;
import com.bookService.core.domain.payment.entity.PaymentOrder;
import com.bookService.core.domain.payment.entity.PaymentOrderHistory;
import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentEventRepository;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentOrderHistoryRepository;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
class PaymentCleanupServiceTest {

    /*
    @Autowired
    PaymentCleanupService cleanupService;

    @Autowired
    SpringDataJpaPaymentEventRepository eventRepository;
    @Autowired
    SpringDataJpaPaymentOrderRepository orderRepository;
    @Autowired
    SpringDataJpaPaymentOrderHistoryRepository historyRepository;
    */
    /*
    @BeforeEach
    void setUp() {
        // --- 간단한 더미 데이터 생성 ---
        PaymentEvent event = PaymentEvent.builder()
                .buyerId(1L)
                .orderName("test-order")
                .paymentStatus(PaymentStatus.NOT_STARTED)
                .isPaymentDone(false)
                .build();
        event = eventRepository.save(event);

        PaymentOrder order = PaymentOrder.builder()
                .paymentEvent(event)
                .productId("BOOK-001")
                .amount(1000)
                .paymentStatus(PaymentStatus.NOT_STARTED)
                .build();
        order = orderRepository.save(order);

        PaymentOrderHistory history = PaymentOrderHistory.builder()
                .paymentOrder(order)
                .previousStatus(PaymentStatus.NOT_STARTED)
                .newStatus(PaymentStatus.SUCCESS)
                .build();
        historyRepository.save(history);
    }
*/
    /*
    @Test
    @DisplayName("deleteAllPayments() 호출 시 모든 Payment 관련 데이터가 삭제된다")
    void deleteAllPayments() {
        // given : setUp() 에서 데이터 1건씩 저장됨
        assertThat(eventRepository.count()).isEqualTo(1);
        assertThat(orderRepository.count()).isEqualTo(1);
        assertThat(historyRepository.count()).isEqualTo(1);

        // when
        cleanupService.deleteAllPayments();

        // then : 모두 삭제되었는지 검증
        assertThat(historyRepository.count()).isZero();
        assertThat(orderRepository.count()).isZero();
        assertThat(eventRepository.count()).isZero();
    }
*/
}