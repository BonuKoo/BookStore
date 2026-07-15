package com.bookService.core.domain.payment.outbox;

import com.bookService.core.domain.login.entity.AccountEntity;
import com.bookService.core.domain.login.repository.AccountJpaRepository;
import com.bookService.core.domain.payment.dto.PaymentExtraDetails;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * M1 Transactional Outbox 계약 검증. DispatchEventMessagePort를 mock으로 대체하므로
 * RabbitMQ 브로커 없이 동작한다. 실 커밋/롤백을 검증하려고 테스트를 @Transactional로 감싸지
 * 않고 로컬 MySQL에 실데이터를 만들었다가 @AfterEach에서 지운다.
 *
 * 검증 포인트:
 *  1. SUCCESS 커밋 → outbox 행이 생기고, AFTER_COMMIT 즉시발행이 성공하면 SUCCESS로 마킹된다.
 *  2. 발행이 실패하면 outbox 행이 FAILURE로 남아 릴레이 재발행 대상이 된다.
 *  3. 트랜잭션 롤백 → outbox 행도 함께 롤백되어 남지 않는다(비즈니스 데이터와 원자성).
 *  4. 릴레이 스케줄러가 미확정(INIT/FAILURE) 행을 재발행하고 SUCCESS로 마킹한다.
 */
@SpringBootTest
class PaymentOutboxIntegrationTest {

    @Autowired private PaymentStatusUpdateRepository paymentStatusUpdateRepository;
    @Autowired private SpringDataJpaPaymentEventRepository paymentEventRepository;
    @Autowired private AccountJpaRepository accountJpaRepository;
    @Autowired private PaymentEventMessageRelayService relayService;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private JdbcTemplate jdbcTemplate;

    @MockitoBean private DispatchEventMessagePort dispatchEventMessagePort;

    private TransactionTemplate transactionTemplate;
    private String orderId;
    private Long accountId;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        orderId = "outbox-test-" + UUID.randomUUID();

        transactionTemplate.executeWithoutResult(status -> {
            AccountEntity account = new AccountEntity("outbox-user-" + UUID.randomUUID(), "pw");
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
        jdbcTemplate.update("DELETE FROM outbox WHERE idempotency_key = ?", orderId);
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
    @DisplayName("SUCCESS 커밋 → outbox 행 저장 + 즉시발행 성공 시 SUCCESS 마킹")
    void successCommit_persistsOutboxAndMarksSent() {
        paymentStatusUpdateRepository.updatePaymentStatus(successCommand());

        verify(dispatchEventMessagePort, atLeastOnce()).dispatch(any());

        String status = outboxStatus();
        String type = jdbcTemplate.queryForObject(
                "SELECT type FROM outbox WHERE idempotency_key = ?", String.class, orderId);
        String payload = jdbcTemplate.queryForObject(
                "SELECT payload FROM outbox WHERE idempotency_key = ?", String.class, orderId);

        assertThat(status).isEqualTo(OutboxStatus.SUCCESS.name());
        assertThat(type).isEqualTo(PaymentEventMessageType.PAYMENT_CONFIRMATION_SUCCESS.name());
        assertThat(payload).contains(orderId);
    }

    @Test
    @DisplayName("즉시발행 실패 → outbox 행이 FAILURE로 남아 릴레이 대상이 된다")
    void dispatchFailure_marksOutboxAsFailure() {
        doThrow(new RuntimeException("broker down")).when(dispatchEventMessagePort).dispatch(any());

        paymentStatusUpdateRepository.updatePaymentStatus(successCommand());

        assertThat(outboxCount()).isEqualTo(1);
        assertThat(outboxStatus()).isEqualTo(OutboxStatus.FAILURE.name());
    }

    @Test
    @DisplayName("트랜잭션 롤백 → outbox 행도 함께 롤백되어 남지 않는다")
    void rollback_persistsNoOutboxRow() {
        transactionTemplate.executeWithoutResult(status -> {
            paymentStatusUpdateRepository.updatePaymentStatus(successCommand());
            status.setRollbackOnly();
        });

        assertThat(outboxCount()).isZero();
    }

    @Test
    @DisplayName("릴레이 스케줄러 → 미확정(INIT) 행을 재발행하고 SUCCESS로 마킹한다")
    void relay_reDispatchesPendingRowAndMarksSent() {
        // 유예시간(10s) 이전에 생성된 INIT 행을 직접 심는다 → 릴레이의 재발행 대상.
        jdbcTemplate.update(
                "INSERT INTO outbox (idempotency_key, status, type, payload, metadata, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                orderId,
                OutboxStatus.INIT.name(),
                PaymentEventMessageType.PAYMENT_CONFIRMATION_SUCCESS.name(),
                "{\"orderId\":\"" + orderId + "\"}",
                "{\"source\":\"core-spa\"}",
                LocalDateTime.now().minusMinutes(1));

        relayService.relay(); // @Async — 별도 스레드에서 실행되므로 결과를 폴링한다.

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(outboxStatus()).isEqualTo(OutboxStatus.SUCCESS.name()));

        verify(dispatchEventMessagePort, atLeastOnce()).dispatch(any());
    }

    private String outboxStatus() {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM outbox WHERE idempotency_key = ?", String.class, orderId);
    }

    private Integer outboxCount() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM outbox WHERE idempotency_key = ?", Integer.class, orderId);
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
