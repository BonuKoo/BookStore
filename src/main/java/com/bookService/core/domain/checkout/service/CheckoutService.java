package com.bookService.core.domain.checkout.service;

import com.bookService.core.domain.cartitem.CartItem;
import com.bookService.core.domain.cartitem.repository.CartItemRepository;
import com.bookService.core.domain.checkout.dto.CheckoutCommand;
import com.bookService.core.domain.checkout.dto.CheckoutResult;
import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import com.bookService.core.domain.payment.entity.PaymentEvent;
import com.bookService.core.domain.payment.entity.PaymentOrder;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentEventRepository;
import com.bookService.core.domain.payment.usecase.CheckoutUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckoutService implements CheckoutUseCase {

    private final SpringDataJpaPaymentEventRepository paymentEventRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public CheckoutResult checkout(CheckoutCommand command) {
        //  Command를 통해 Cart data를 가져온다.
        // Checkout시, 컨트롤러에서 넘어 온 CartId와, cart에 담긴 Item id들
        Long cartId = command.getCartId();

        // 1. 중복 주문을 방지하기 위해, PaymentEvent가 있는지 사전 조회
        Optional<PaymentEvent> paymentEventOptional = paymentEventRepository.findByOrderId(command.getIdempotencyKey());

        if (paymentEventOptional.isPresent()){
            PaymentEvent paymentEvent = paymentEventOptional.get();
            return new CheckoutResult(
                    paymentEvent.totalAmount(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getOrderName());
        }

        // 2. 새 PaymentEvent 생성 & 저장
        PaymentEvent paymentEvent = createPaymentEvent(command);

            paymentEventRepository.save(paymentEvent);
            /*
            try {


        } catch (DataIntegrityViolationException e) {
            // ✅ DB UNIQUE 제약 조건 위반 (동시성으로 인한 중복 insert 시)
            if (e.getCause() instanceof ConstraintViolationException) {
            // 🔑 중복 발생 → 세션 초기화
            entityManager.clear();
                PaymentEvent committed = waitForCommitted(command.getIdempotencyKey());
                return new CheckoutResult(
                        committed.totalAmount(),
                        committed.getOrderId(),
                        committed.getOrderName()
                );
            }
            throw e; // 다른 DB 예외는 그대로 던짐
        }
        */
        return new CheckoutResult(
                paymentEvent.totalAmount(),
                paymentEvent.getOrderId(),
                paymentEvent.getOrderName()
        );
    }


    private PaymentEvent createPaymentEvent(CheckoutCommand command) {

        List<Long> cartItemIds = command.getCartItemIds();
        List<CartItem> cartItems = cartItemRepository.findAllWithItemByIdIn(cartItemIds);

        List<PaymentOrder> paymentOrders = cartItems.stream()
                .map(cartItem -> PaymentOrder.builder()
                        .sellerId(cartItem.getItem().getSellerId())
                        .orderId(command.getIdempotencyKey())
                        .productId(cartItem.getItem().getIsbn())
                        .amount(cartItem.getItem().getPrice() * cartItem.getAmount())
                        .paymentStatus(PaymentStatus.NOT_STARTED)
                        .build()
                )
                .collect(Collectors.toList());

        // 주문 이름
        String orderName = cartItems.stream()
                .map(orderItem -> orderItem.getItem().getTitle())
                .collect(Collectors.joining(", "));

        PaymentEvent build = PaymentEvent.builder()
                .buyerId(command.getBuyerId())
                .orderId(command.getIdempotencyKey())
                .orderName(orderName)
                .paymentOrders(paymentOrders)
                .build();

        for (PaymentOrder order : paymentOrders) {
            order.setPaymentEvent(build);
        }

        return build;
    }

    /**
     * Transaction 범위 재조정
     * findByOrderId 호출은 DB 상태를 변경하지 않는 읽기 전용이므로, 트랜잭션 범위에 포함될 필요가 없다.

     * -> SAVE() 메서드 호출 부분만 감싸도록 분리한다.
     * findByOrderId()는 트랜잭션이 없는 상태에서 호출되므로, 불필요한 DB 커넥션 점유 및 트랜잭션 오버헤드를 줄일 수 있다.
     */
    @Override
    public CheckoutResult checkout2(CheckoutCommand command) {
        Long cartId = command.getCartId();

        // 1. 중복 주문을 방지하기 위해, PaymentEvent가 있는지 사전 조회
        Optional<PaymentEvent> paymentEventOptional = paymentEventRepository.findByOrderId(command.getIdempotencyKey());

        if (paymentEventOptional.isPresent()){
            PaymentEvent paymentEvent = paymentEventOptional.get();
            return new CheckoutResult(
                    paymentEvent.totalAmount(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getOrderName());
        }

        PaymentEvent paymentEvent = savePaymentEvent(command);

        return new CheckoutResult(
                paymentEvent.totalAmount(),
                paymentEvent.getOrderId(),
                paymentEvent.getOrderName()
        );
    }

    @Transactional
    public PaymentEvent savePaymentEvent(CheckoutCommand command) {
        // 2. 새 PaymentEvent 생성
        PaymentEvent paymentEvent = createPaymentEvent2(command);

        // 3. PaymentEvent 저장 (트랜잭션 범위에 포함)
        paymentEventRepository.save(paymentEvent);

        return paymentEvent;
    }

    private PaymentEvent createPaymentEvent2(CheckoutCommand command) {

        List<Long> cartItemIds = command.getCartItemIds();
        List<CartItem> cartItems = cartItemRepository.findAllWithItemByIdIn(cartItemIds);

        List<PaymentOrder> paymentOrders = cartItems.stream()
                .map(cartItem -> PaymentOrder.builder()
                        .sellerId(cartItem.getItem().getSellerId())
                        .orderId(command.getIdempotencyKey())
                        .productId(cartItem.getItem().getIsbn())
                        .amount(cartItem.getItem().getPrice() * cartItem.getAmount())
                        .paymentStatus(PaymentStatus.NOT_STARTED)
                        .build()
                )
                .collect(Collectors.toList());

        // 주문 이름
        String orderName = cartItems.stream()
                .map(orderItem -> orderItem.getItem().getTitle())
                .collect(Collectors.joining(", "));

        PaymentEvent build = PaymentEvent.builder()
                .buyerId(command.getBuyerId())
                .orderId(command.getIdempotencyKey())
                .orderName(orderName)
                .paymentOrders(paymentOrders)
                .build();

        for (PaymentOrder order : paymentOrders) {
            order.setPaymentEvent(build);
        }

        return build;
    }


    /*
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    private PaymentEvent waitForCommitted(String orderId) {
        final int MAX_RETRY = 10;       // 최대 5회
        final long SLEEP_MS = 100L;     // 50ms 간격 (총 최대 250ms 대기)

        for (int i = 0; i < MAX_RETRY; i++) {
            Optional<PaymentEvent> found = paymentEventRepository.findByOrderId(orderId);
            if (found.isPresent()) {
                return found.get();
            }
            try {
                Thread.sleep(SLEEP_MS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break; // 인터럽트가 들어오면 즉시 종료
            }
        }
        throw new IllegalStateException(
                "Duplicate but not found after " + MAX_RETRY + " retries (orderId=" + orderId + ")");
    }
*/
}
