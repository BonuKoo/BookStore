package com.bookService.core.infra.toss.service;

import com.bookService.core.common.util.IdempotencyCreator;
import com.bookService.core.domain.cart.service.CartService;
import com.bookService.core.domain.cartitem.dto.CartListDTOForQueryProjection;
import com.bookService.core.domain.cartitem.repository.CartItemRepository;
import com.bookService.core.domain.checkout.dto.CheckoutCommand;
import com.bookService.core.domain.checkout.dto.CheckoutRequest;
import com.bookService.core.domain.checkout.dto.CheckoutResult;
import com.bookService.core.domain.checkout.service.CheckoutService;
import com.bookService.core.domain.payment.dto.*;
import com.bookService.core.domain.payment.entity.PaymentEvent;
import com.bookService.core.domain.payment.entity.PaymentOrder;
import com.bookService.core.domain.payment.entity.PaymentOrderHistory;
import com.bookService.core.domain.payment.enumtype.PSPConfirmationStatus;
import com.bookService.core.domain.payment.enumtype.PaymentMethod;
import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import com.bookService.core.domain.payment.enumtype.PaymentType;
import com.bookService.core.domain.payment.persistent.repository.PaymentOrderHistoryRepository;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentEventRepository;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentOrderHistoryRepository;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentOrderRepository;
import com.bookService.core.infra.toss.executor.TossPaymentExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

//@SpringBootTest
@AutoConfigureMockMvc
@Import(TossPaymentExecutor.class)
class PaymentConfirmServiceTest {


    @Autowired private CheckoutService checkoutService;
    @Autowired private PaymentConfirmService confirmService;
    @Autowired private CartService cartService;
    @Autowired private SpringDataJpaPaymentEventRepository paymentEventRepository;
    @Autowired private SpringDataJpaPaymentOrderRepository paymentOrderRepository;
    @Autowired private SpringDataJpaPaymentOrderHistoryRepository paymentOrderHistoryRepository;
    @Autowired private CartItemRepository cartItemRepository;

    @MockitoBean
    private TossPaymentExecutor tossPaymentExecutor;

    private Long testUserId1;
    private List<CartListDTOForQueryProjection> cartItemList;
    private CheckoutCommand command;

//    @BeforeEach
    void setUp(){
        // 데이터 준비
        String userId = "1040";
        List<CartListDTOForQueryProjection> cartItemList = cartService.getCartItemList(userId);

        List<Long> cartItemIds = cartItemList.stream()
                .map(CartListDTOForQueryProjection::getCartItemId)
                .toList();

        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setCartItemIds(cartItemIds);

        String idempotencyKey = IdempotencyCreator.create(checkoutRequest);
        Long cartId = cartService.findCartByAccountIdStringType(userId);

        command = CheckoutCommand.builder()
                .cartId(cartId)
                .buyerId(Long.parseLong(userId))
                .cartItemIds(checkoutRequest.getCartItemIds())
                .idempotencyKey("test-key"+IdempotencyCreator.create(checkoutRequest))
                .build();
    }

    /**
     * PaymentOrderHistory는 updatePaymentStatusToSuccess 내부에서 insertPaymentHistory 호출로 생성됨 → DB 저장 확인 가능
     * 이전 상태(previousStatus)와 변경 후 상태(newStatus)를 검증
     * Reason 필드에 "PAYMENT_CONFIRMATION" 관련 문자열 포함 여부 체크 → 이력 로그가 올바르게 남았는지 검증.
     * */

//    @Test
    void shouldConfirmPaymentAndMarkSuccess_inRealDb() {
        // 1. Checkout 실행
        CheckoutResult checkoutResult = checkoutService.checkout(command);

        // 2. PaymentConfirmCommand 생성
        PaymentConfirmCommand paymentConfirmCommand = new PaymentConfirmCommand(
                UUID.randomUUID().toString(),
                checkoutResult.getOrderId(),
                checkoutResult.getAmount()
        );

        // 3. TossPaymentExecutor Stub 사용 (실제 결제 호출 막기)

        PaymentExecutionResult mockResult = new PaymentExecutionResult(
                paymentConfirmCommand.getPaymentKey(),
                paymentConfirmCommand.getOrderId(),
                PaymentExtraDetails.builder()
                        .type(PaymentType.NORMAL)
                        .method(PaymentMethod.EASY_PAY)
                        .totalAmount(paymentConfirmCommand.getAmount())
                        .orderName("test_order_name")
                        .pspConfirmationStatus(PSPConfirmationStatus.DONE)
                        .approvedAt(LocalDateTime.now())
                        .pspRawData("{}")
                        .build(),
                null,  // failure
                true,  // isSuccess
                false, // isFailure
                false, // isUnknown
                false  // isRetryable
        );

        when(tossPaymentExecutor.execute(paymentConfirmCommand)).thenReturn(mockResult);


        // 4. PaymentConfirm 수행
        PaymentConfirmationResult result = confirmService.confirm(paymentConfirmCommand);

        // 5. 결과 검증
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);


        // 6. PaymentOrder 상태 검증
        List<PaymentOrder> orders = paymentOrderRepository.findListPaymentOrderByIdempotencyKey(checkoutResult.getOrderId());
        for (PaymentOrder order : orders) {
            assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        }

        // 7. PaymentOrderHistory 생성 검증
        List<PaymentOrderHistory> histories = paymentOrderHistoryRepository.findAll();

        assertThat(histories).isNotEmpty();

        histories.forEach(history -> {
            assertThat(history.getPaymentOrder()).isNotNull();
            assertThat(history.getPreviousStatus()).isNotEqualTo(history.getNewStatus());

            if (history.getNewStatus() == PaymentStatus.EXECUTING) {
                assertThat(history.getReason()).isEqualTo("PAYMENT_CONFIRMATION_START");
            } else if (history.getNewStatus() == PaymentStatus.SUCCESS) {
                assertThat(history.getReason()).isEqualTo("PAYMENT_CONFIRMATION_DONE");
            }
        });

    }

//    @Test
    void shouldConfirmPaymentAndMarkFailure_inRealDb() {
        // 1. Checkout 실행
        CheckoutResult checkoutResult = checkoutService.checkout(command);

        // 2. PaymentConfirmCommand 생성
        PaymentConfirmCommand paymentConfirmCommand = new PaymentConfirmCommand(
                UUID.randomUUID().toString(),
                checkoutResult.getOrderId(),
                checkoutResult.getAmount()
        );

        // 3. TossPaymentExecutor Stub 사용 (실제 결제 호출 막기)

        PaymentExecutionResult mockFailureResult = new PaymentExecutionResult(
                paymentConfirmCommand.getPaymentKey(),
                paymentConfirmCommand.getOrderId(),
                PaymentExtraDetails.builder()
                        .type(PaymentType.NORMAL)
                        .method(PaymentMethod.EASY_PAY)
                        .totalAmount(paymentConfirmCommand.getAmount())
                        .orderName("test_order_name")
                        .pspConfirmationStatus(PSPConfirmationStatus.DONE)
                        .approvedAt(LocalDateTime.now())
                        .pspRawData("{}")
                        .build(),
                new PaymentFailure("ERROR", "Test Error"), // 실패 정보
                false, // isSuccess
                true,  // isFailure
                false, // isUnknown
                false  // isRetryable
        );

        when(tossPaymentExecutor.execute(paymentConfirmCommand)).thenReturn(mockFailureResult);


        // 4. PaymentConfirm 수행
        PaymentConfirmationResult paymentConfirmationResult = confirmService.confirm(paymentConfirmCommand);

        // 5. 결과 검증
        assertThat(paymentConfirmationResult.getStatus()).isEqualTo(PaymentStatus.FAILURE);
        assertThat(paymentConfirmationResult.getFailure().getErrorCode())
                .isEqualTo("ERROR");
        assertThat(paymentConfirmationResult.getMessage())
                .isEqualTo("결제 처리에 실패하였습니다.");


        // 6. PaymentOrder 상태 검증
        List<PaymentOrder> orders = paymentOrderRepository.findListPaymentOrderByIdempotencyKey(checkoutResult.getOrderId());
        for (PaymentOrder order : orders) {
            assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.FAILURE);
        }

        // 7. PaymentOrderHistory 검증
        List<PaymentOrderHistory> histories = paymentOrderHistoryRepository.findAll();
        histories.forEach(history -> {
            if (history.getNewStatus() == PaymentStatus.EXECUTING) {
                assertThat(history.getReason()).isEqualTo("PAYMENT_CONFIRMATION_START");
            } else if (history.getNewStatus() == PaymentStatus.FAILURE) {
                assertThat(history.getReason()).isNotBlank();
//                System.out.println(history.getReason().toString());
            }
        });
    }


}