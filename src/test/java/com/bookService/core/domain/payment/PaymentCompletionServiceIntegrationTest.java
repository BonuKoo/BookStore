package com.bookService.core.domain.payment;

import com.bookService.core.domain.login.entity.AccountEntity;
import com.bookService.core.domain.login.repository.AccountJpaRepository;
import com.bookService.core.domain.payment.entity.PaymentEvent;
import com.bookService.core.domain.payment.entity.PaymentOrder;
import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * M4 완결 수신부의 핵심 계약 검증: wallet/ledger 완결 통지가 각각 도착할 때마다
 * 해당 플래그만 갱신되고, 두 플래그가 모두 참이 됐을 때만 결제가 최종 완결된다.
 */
@SpringBootTest
class PaymentCompletionServiceIntegrationTest {

    @Autowired private PaymentCompletionService paymentCompletionService;
    @Autowired private SpringDataJpaPaymentEventRepository paymentEventRepository;
    @Autowired private AccountJpaRepository accountJpaRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private String orderId;
    private Long accountId;

    @BeforeEach
    void setUp() {
        orderId = "m4-test-" + UUID.randomUUID();

        AccountEntity account = new AccountEntity("m4-test-user-" + UUID.randomUUID(), "pw");
        accountJpaRepository.save(account);
        accountId = account.getId();

        PaymentOrder order1 = PaymentOrder.builder()
                .sellerId(101L).productId("isbn-001").orderId(orderId)
                .amount(12000).paymentStatus(PaymentStatus.SUCCESS)
                .build();
        PaymentOrder order2 = PaymentOrder.builder()
                .sellerId(102L).productId("isbn-002").orderId(orderId)
                .amount(8000).paymentStatus(PaymentStatus.SUCCESS)
                .build();

        PaymentEvent event = PaymentEvent.builder()
                .buyerId(account.getId())
                .accountEntity(account)
                .orderId(orderId)
                .orderName("M4 테스트 도서 외 1건")
                .paymentOrders(List.of(order1, order2))
                .build();
        order1.setPaymentEvent(event);
        order2.setPaymentEvent(event);

        paymentEventRepository.save(event);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM payment_orders WHERE order_id = ?", orderId);
        jdbcTemplate.update("DELETE FROM payment_event WHERE order_id = ?", orderId);
        if (accountId != null) {
            jdbcTemplate.update("DELETE FROM account_entity WHERE id = ?", accountId);
        }
    }

    private int walletUpdatedCount() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payment_orders WHERE order_id = ? AND is_wallet_updated = true",
                Integer.class, orderId);
    }

    private int ledgerUpdatedCount() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payment_orders WHERE order_id = ? AND is_ledger_updated = true",
                Integer.class, orderId);
    }

    private boolean paymentDone() {
        return jdbcTemplate.queryForObject(
                "SELECT is_payment_done FROM payment_event WHERE order_id = ?", Boolean.class, orderId);
    }

    @Test
    @DisplayName("정산 완결 통지만 도착 — 지갑 플래그만 갱신되고 결제는 아직 완결되지 않는다")
    void confirmWalletUpdated_onlyMarksWalletFlag_paymentNotDoneYet() {
        paymentCompletionService.confirmWalletUpdated(orderId);

        assertThat(walletUpdatedCount()).isEqualTo(2);
        assertThat(ledgerUpdatedCount()).isZero();
        assertThat(paymentDone()).isFalse();
    }

    @Test
    @DisplayName("정산+장부 완결 통지가 모두 도착하면 결제가 최종 완결된다")
    void confirmBoth_completesPayment() {
        paymentCompletionService.confirmWalletUpdated(orderId);
        paymentCompletionService.confirmLedgerUpdated(orderId);

        assertThat(walletUpdatedCount()).isEqualTo(2);
        assertThat(ledgerUpdatedCount()).isEqualTo(2);
        assertThat(paymentDone()).isTrue();
    }

    @Test
    @DisplayName("순서가 반대(장부 먼저, 정산 나중)여도 결제가 완결된다")
    void confirmLedgerThenWallet_completesPayment() {
        paymentCompletionService.confirmLedgerUpdated(orderId);
        assertThat(paymentDone()).isFalse();

        paymentCompletionService.confirmWalletUpdated(orderId);
        assertThat(paymentDone()).isTrue();
    }

    @Test
    @DisplayName("멱등성: 같은 통지가 중복 전달돼도 에러 없이 동일한 결과를 유지한다")
    void confirmWalletUpdated_duplicateNotification_idempotent() {
        paymentCompletionService.confirmWalletUpdated(orderId);
        paymentCompletionService.confirmWalletUpdated(orderId); // 재전달 시뮬레이션

        assertThat(walletUpdatedCount()).isEqualTo(2);
        assertThat(paymentDone()).isFalse();
    }

    @Test
    @DisplayName("매칭되는 결제 이벤트가 없는 orderId — 예외 없이 무시된다")
    void confirmWalletUpdated_unknownOrderId_doesNotThrow() {
        assertThatCode(() -> paymentCompletionService.confirmWalletUpdated("no-such-order-" + UUID.randomUUID()))
                .doesNotThrowAnyException();
    }
}
