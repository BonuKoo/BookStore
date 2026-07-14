package com.bookService.core.domain.payment;

import com.bookService.core.domain.login.entity.AccountEntity;
import com.bookService.core.domain.login.repository.AccountJpaRepository;
import com.bookService.core.domain.payment.dto.PaymentExtraDetails;
import com.bookService.core.domain.payment.dto.PaymentFailure;
import com.bookService.core.domain.payment.dto.PaymentStatusUpdateCommand;
import com.bookService.core.domain.payment.entity.PaymentEvent;
import com.bookService.core.domain.payment.entity.PaymentOrder;
import com.bookService.core.domain.payment.enumtype.PSPConfirmationStatus;
import com.bookService.core.domain.payment.enumtype.PaymentEventMessageType;
import com.bookService.core.domain.payment.enumtype.PaymentMethod;
import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import com.bookService.core.domain.payment.enumtype.PaymentType;
import com.bookService.core.domain.payment.persistent.PaymentStatusUpdateRepository;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentEventRepository;
import com.bookService.core.domain.payment.port.DispatchEventMessagePort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Phase 2 프로듀서의 핵심 계약 검증:
 *  - 결제 상태 전이 트랜잭션이 "커밋"됐을 때만 메시지가 브로커로 나간다.
 *  - 롤백되면 절대 나가지 않는다. (AFTER_COMMIT 발행 설계의 존재 이유)
 *
 * DispatchEventMessagePort를 mock으로 대체하므로 RabbitMQ 브로커 없이 동작한다.
 * 단, 실제 커밋/롤백을 검증해야 하므로 테스트 자체는 @Transactional로 감싸지 않고
 * 로컬 MySQL(core2_spa)에 실데이터를 만들었다가 @AfterEach에서 지운다.
 */
@SpringBootTest
class PaymentEventMessagePublishIntegrationTest {

    @Autowired private PaymentStatusUpdateRepository paymentStatusUpdateRepository;
    @Autowired private SpringDataJpaPaymentEventRepository paymentEventRepository;
    @Autowired private AccountJpaRepository accountJpaRepository;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private JdbcTemplate jdbcTemplate;

    @MockitoBean private DispatchEventMessagePort dispatchEventMessagePort;

    private TransactionTemplate transactionTemplate;
    private String orderId;
    private Long accountId;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        orderId = "mq-test-" + UUID.randomUUID();

        // 계정 + PaymentEvent(+주문 2건, 판매자 2명)를 커밋된 상태로 준비
        transactionTemplate.executeWithoutResult(status -> {
            AccountEntity account = new AccountEntity("mq-test-user-" + UUID.randomUUID(), "pw");
            accountJpaRepository.save(account);
            accountId = account.getId();

            PaymentOrder order1 = PaymentOrder.builder()
                    .sellerId(101L).productId("isbn-001").orderId(orderId)
                    .amount(12000).paymentStatus(PaymentStatus.EXECUTING)
                    .build();
            PaymentOrder order2 = PaymentOrder.builder()
                    .sellerId(102L).productId("isbn-002").orderId(orderId)
                    .amount(8000).paymentStatus(PaymentStatus.EXECUTING)
                    .build();

            PaymentEvent event = PaymentEvent.builder()
                    .buyerId(account.getId())
                    .accountEntity(account)
                    .orderId(orderId)
                    .orderName("테스트 도서 외 1건")
                    .paymentOrders(List.of(order1, order2))
                    .build();
            order1.setPaymentEvent(event);
            order2.setPaymentEvent(event);

            paymentEventRepository.save(event);
        });
    }

    @AfterEach
    void tearDown() {
        // FK 역순 정리: history → orders → event → account
        jdbcTemplate.update(
                "DELETE poh FROM payment_order_history poh " +
                "JOIN payment_orders po ON poh.payment_order_id = po.payment_order_id " +
                "WHERE po.order_id = ?", orderId);
        jdbcTemplate.update("DELETE FROM payment_orders WHERE order_id = ?", orderId);
        jdbcTemplate.update("DELETE FROM payment_event WHERE order_id = ?", orderId);
        if (accountId != null) {
            jdbcTemplate.update("DELETE FROM account_entity WHERE id = ?", accountId);
        }
    }

    @Test
    @DisplayName("SUCCESS 전이 커밋 → payment.confirmed 메시지가 정확히 1건 발행된다")
    void successCommit_dispatchesConfirmedMessageOnce() {
        paymentStatusUpdateRepository.updatePaymentStatus(successCommand());

        ArgumentCaptor<PaymentEventMessage> captor = ArgumentCaptor.forClass(PaymentEventMessage.class);
        verify(dispatchEventMessagePort, times(1)).dispatch(captor.capture());

        PaymentEventMessage message = captor.getValue();
        assertThat(message.getMessageType()).isEqualTo(PaymentEventMessageType.PAYMENT_CONFIRMATION_SUCCESS);

        Map<String, Object> payload = message.getPayload();
        assertThat(payload.get("orderId")).isEqualTo(orderId);
        assertThat(payload.get("totalAmount")).isEqualTo(20000L);
        assertThat(payload.get("buyerName")).asString().startsWith("mq-test-user-");
        assertThat((List<?>) payload.get("items")).hasSize(2);

        assertThat(message.getMetadata().get("source")).isEqualTo("core-spa");
    }

    @Test
    @DisplayName("트랜잭션 롤백 → 상태 전이가 취소되고 메시지는 발행되지 않는다")
    void rollback_dispatchesNothing() {
        transactionTemplate.executeWithoutResult(status -> {
            paymentStatusUpdateRepository.updatePaymentStatus(successCommand());
            status.setRollbackOnly();
        });

        verify(dispatchEventMessagePort, never()).dispatch(org.mockito.ArgumentMatchers.any());

        // 롤백이므로 주문 상태도 EXECUTING 그대로여야 한다
        Integer successCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payment_orders WHERE order_id = ? AND payment_status = 'SUCCESS'",
                Integer.class, orderId);
        assertThat(successCount).isZero();
    }

    @Test
    @DisplayName("FAILURE 전이 커밋 → payment.failed 메시지가 errorCode와 함께 발행된다")
    void failureCommit_dispatchesFailedMessage() {
        PaymentStatusUpdateCommand command = PaymentStatusUpdateCommand.builder()
                .paymentKey("test-payment-key")
                .orderId(orderId)
                .status(PaymentStatus.FAILURE)
                .failure(new PaymentFailure("REJECT_CARD_COMPANY", "카드사 거절"))
                .build();

        paymentStatusUpdateRepository.updatePaymentStatus(command);

        ArgumentCaptor<PaymentEventMessage> captor = ArgumentCaptor.forClass(PaymentEventMessage.class);
        verify(dispatchEventMessagePort, times(1)).dispatch(captor.capture());

        PaymentEventMessage message = captor.getValue();
        assertThat(message.getMessageType()).isEqualTo(PaymentEventMessageType.PAYMENT_CONFIRMATION_FAILURE);
        assertThat(message.getPayload().get("orderId")).isEqualTo(orderId);
        assertThat(message.getPayload().get("errorCode")).isEqualTo("REJECT_CARD_COMPANY");
    }

    private PaymentStatusUpdateCommand successCommand() {
        PaymentExtraDetails details = PaymentExtraDetails.builder()
                .type(PaymentType.NORMAL)
                .method(PaymentMethod.EASY_PAY)
                .approvedAt(LocalDateTime.now())
                .orderName("테스트 도서 외 1건")
                .pspConfirmationStatus(PSPConfirmationStatus.DONE)
                .totalAmount(20000L)
                .pspRawData("{}")
                .build();

        return PaymentStatusUpdateCommand.builder()
                .paymentKey("test-payment-key")
                .orderId(orderId)
                .status(PaymentStatus.SUCCESS)
                .extraDetails(details)
                .build();
    }
}
