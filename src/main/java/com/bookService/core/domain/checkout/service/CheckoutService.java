package com.bookService.core.domain.checkout.service;

import com.bookService.core.common.exception.checkout.CheckoutException;
import com.bookService.core.common.util.IdempotencyCreator;
import com.bookService.core.domain.cartitem.dto.CheckoutItemForQueryProjection2;
import com.bookService.core.domain.cartitem.repository.CartItemRepository;
import com.bookService.core.domain.checkout.dto.CheckoutCommandForDev;
import com.bookService.core.domain.checkout.dto.CheckoutRequest;
import com.bookService.core.domain.checkout.dto.CheckoutResult;
import com.bookService.core.domain.checkout.repository.CheckoutTransactionRepository;
import com.bookService.core.domain.payment.dto.PaymentCheckoutOptDtoForQueryProjection;
import com.bookService.core.domain.payment.entity.PaymentEvent;
import com.bookService.core.domain.payment.entity.PaymentOrder;
import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentEventRepository;
import com.bookService.core.domain.payment.usecase.CheckoutFindExistingOrderUseCase;
import com.bookService.core.domain.payment.usecase.CheckoutUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService implements CheckoutUseCase {

    private final SpringDataJpaPaymentEventRepository paymentEventRepository;
    private final CartItemRepository cartItemRepository;
    private final CheckoutFindExistingOrderUseCase checkoutFindExistingOrderService;
    private final CheckoutTransactionRepository transactionRepository;

    @Override
    public CheckoutResult checkout(String userId, CheckoutRequest request) throws CheckoutException {
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);
        PaymentEvent paymentEvent = createPaymentEvent(checkoutCommandForDev);

        try{
            paymentEventRepository.save(paymentEvent);
            return CheckoutResult.created(paymentEvent);
        } catch (DataIntegrityViolationException ex) {
            if (isDuplicateKeyError(ex)) {

                Optional<PaymentCheckoutOptDtoForQueryProjection> exist =
                        checkoutFindExistingOrderService.findExistingOrder(paymentEvent.getOrderId());

                return CheckoutResult.alreadyExists(paymentEvent);
            }
            throw new CheckoutException("DB 오류", ex);
        }
    }

    private boolean isDuplicateKeyError(DataIntegrityViolationException ex) {
        Throwable cause = ex.getRootCause();
        return (cause instanceof SQLIntegrityConstraintViolationException);
    }

    private static CheckoutCommandForDev getCheckoutCommand(String userId, CheckoutRequest request) {
        Long userIdLongType = Long.parseLong(userId);
        CheckoutCommandForDev checkoutCommandForDev = CheckoutCommandForDev.builder()
                .buyerId(userIdLongType)
                .cartItemIds(request.getCartItemIds())
                // order_id로 사용될 멱등성 키를 생성합니다.
                .idempotencyKey(IdempotencyCreator.create(request, userId))
                .build();

        return checkoutCommandForDev;
    }

    private PaymentEvent createPaymentEvent(CheckoutCommandForDev command)  {

        List<Long> cartItemIds = command.getCartItemIds();

        // payment_event.buyer 컬럼이 NOT NULL이므로 AccountEntity까지 함께 조회해서 세팅해야 insert가 성공한다
        List<CheckoutItemForQueryProjection2> cartItems = cartItemRepository.customCartItemProjection2(cartItemIds);

        PaymentEvent build = getPaymentEventAndOrder(command, cartItems);

        return build;
    }

    private static PaymentEvent getPaymentEventAndOrder(CheckoutCommandForDev command, List<CheckoutItemForQueryProjection2> cartItems) {
        List<PaymentOrder> paymentOrders = cartItems.stream()
                .map(cartItem -> PaymentOrder.builder()
                        .sellerId(cartItem.getSellerId())
                        .orderId(command.getIdempotencyKey())
                        .productId(cartItem.getIsbn())
                        .amount(cartItem.getPrice() * cartItem.getAmount())
                        .quantity(cartItem.getAmount())
                        .paymentStatus(PaymentStatus.NOT_STARTED)
                        .build()
                )
                .collect(Collectors.toList());

        String orderName = cartItems.stream()
                .map(cartItem -> cartItem.getTitle())
                .collect(Collectors.joining(", "));

        PaymentEvent build = PaymentEvent.builder()
                .buyerId(command.getBuyerId())
                .accountEntity(cartItems.getFirst().getAccountEntity())
                .orderId(command.getIdempotencyKey())
                .orderName(orderName)
                .paymentOrders(paymentOrders)
                .build();

        for (PaymentOrder order : paymentOrders) {
            order.setPaymentEvent(build);
        }
        return build;
    }

}
