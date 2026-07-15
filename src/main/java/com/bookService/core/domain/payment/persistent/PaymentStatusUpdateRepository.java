package com.bookService.core.domain.payment.persistent;

import com.bookService.core.common.exception.checkout.PaymentAlreadyProcessedException;
import com.bookService.core.domain.payment.PaymentEventMessage;
import com.bookService.core.domain.payment.enumtype.PaymentEventMessageType;
import com.bookService.core.domain.payment.outbox.PaymentOutboxService;
import com.bookService.core.domain.payment.enumtype.PaymentMethod;
import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import com.bookService.core.domain.payment.dto.PaymentExtraDetails;
import com.bookService.core.domain.payment.dto.PaymentStatusUpdateCommand;
import com.bookService.core.domain.payment.entity.PaymentEvent;
import com.bookService.core.domain.payment.entity.PaymentOrder;
import com.bookService.core.domain.payment.entity.PaymentOrderHistory;
import com.bookService.core.domain.payment.persistent.repository.PaymentEventRepository;
import com.bookService.core.domain.payment.persistent.repository.PaymentOrderHistoryRepository;
import com.bookService.core.domain.payment.persistent.repository.PaymentOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.bookService.core.domain.payment.enumtype.PaymentStatus.*;


@Repository
@RequiredArgsConstructor
public class PaymentStatusUpdateRepository {

    private final PaymentEventRepository paymentEventRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentOrderHistoryRepository paymentOrderHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PaymentOutboxService paymentOutboxService;

    @Transactional // 하나의 트랜잭션으로 묶어야 한다.
    public boolean updatePaymentStatusToExecuting(String orderId, String paymentKey){
        List<PaymentOrder> paymentOrders = checkPreviousPaymentOrderStatus(orderId);
        insertPaymentHistory(paymentOrders, PaymentStatus.EXECUTING,"PAYMENT_CONFIRMATION_START");
        updatePaymentOrderStatus(paymentOrders,PaymentStatus.EXECUTING);
        updatePaymentKey(orderId, paymentKey);
        return true;
    }

    @Transactional
    public boolean updatePaymentStatus(PaymentStatusUpdateCommand command){
        switch (command.getStatus()){
            case SUCCESS :
                return updatePaymentStatusToSuccess(command);
            case FAILURE:
                return updatePaymentStatusToFailure(command);
            case UNKNOWN:
                return updatePaymentStatusToUnknown(command);
            default:
                throw new IllegalArgumentException(
                        "결제 상태 (status: " + command.getStatus() + ") 는 올바르지 않은 결제 상태입니다."
                );
        }
    };

    private List<PaymentOrder> checkPreviousPaymentOrderStatus(String orderId){
        List<PaymentOrder> orders = paymentOrderRepository.findListPaymentOrderByIdempotencyKey(orderId);

        for(PaymentOrder order : orders){
            PaymentStatus status = order.getPaymentStatus();
            if (status == SUCCESS) {
                throw new PaymentAlreadyProcessedException("이미 처리 성공한 결제입니다.", SUCCESS);
            }
            if (status == FAILURE) {
                throw new PaymentAlreadyProcessedException("이미 처리 실패한 결제입니다.", FAILURE);
            }
        }

        return orders;
    }
    // 결제 상태가 변경될 때 이력을 남긴다.
    private void insertPaymentHistory(List<PaymentOrder> orders, PaymentStatus newStatus, String reason){

        List<PaymentOrderHistory> histories = orders.stream()
                .map(order -> PaymentOrderHistory.builder()
                        .paymentOrder(order)
                        .previousStatus(order.getPaymentStatus())
                        .newStatus(newStatus)
                        .createdAt(LocalDateTime.now())
                        .reason(reason)
                        .build()
                ).collect(Collectors.toList());

        paymentOrderHistoryRepository.saveAll(histories);
    }
    // 상태 업데이트
    private void updatePaymentOrderStatus(List<PaymentOrder> orders, PaymentStatus newStatus) {
        for (PaymentOrder order : orders) {
            order.setPaymentStatus(newStatus);
        }
        paymentOrderRepository.saveAll(orders);
    }
    // 키 업데이트
    private void updatePaymentKey(String orderId, String paymentKey) {
        Optional<PaymentEvent> paymentEventOptional = paymentEventRepository.findByOrderId(orderId);
        PaymentEvent paymentEvent = paymentEventOptional
                .orElseThrow((() -> new EntityNotFoundException("결제 이벤트를 찾을 수 없습니다.")));
        paymentEvent.setPaymentKey(paymentKey);
        paymentEventRepository.save(paymentEvent);
    }

    // 트랜잭션 경계는 public updatePaymentStatus()가 잡는다. private 메서드의 @Transactional은
    // 프록시 AOP가 적용되지 않아 무효이므로 제거한다.
    private boolean updatePaymentStatusToSuccess(PaymentStatusUpdateCommand command){
        List<PaymentOrder> orders = paymentOrderRepository.findListPaymentOrderByIdempotencyKey(command.getOrderId());
        insertPaymentHistory(orders, command.getStatus(),"PAYMENT_CONFIRMATION_DONE");
        updatePaymentOrderStatus(orders, command.getStatus());
        PaymentEvent event = updatePaymentEventExtraDetails(command);

        // Transactional Outbox: 발행할 메시지를 같은 트랜잭션에서 outbox에 INIT으로 저장한다.
        // 이후 AFTER_COMMIT 리스너가 즉시 발행하고, 실패하면 릴레이 스케줄러가 재발행한다.
        PaymentEventMessage message = buildSuccessMessage(event, orders);
        paymentOutboxService.insertOutbox(message);
        applicationEventPublisher.publishEvent(message);
        return true;
    }

    private boolean updatePaymentStatusToFailure(PaymentStatusUpdateCommand command){
        List<PaymentOrder> orders = paymentOrderRepository.findListPaymentOrderByIdempotencyKey(command.getOrderId());
        insertPaymentHistory(orders, command.getStatus(), command.getFailure().toString());
        updatePaymentOrderStatus(orders, command.getStatus());

        PaymentEvent event = paymentEventRepository.findByOrderId(command.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("결제 이벤트 없음"));
        PaymentEventMessage message = buildFailureMessage(event, command);
        paymentOutboxService.insertOutbox(message);
        applicationEventPublisher.publishEvent(message);
        return true;
    }
    private  boolean updatePaymentStatusToUnknown(PaymentStatusUpdateCommand command){
        List<PaymentOrder> orders = paymentOrderRepository.findListPaymentOrderByIdempotencyKey(command.getOrderId());
        insertPaymentHistory(orders, command.getStatus(), command.getFailure().toString());
        updatePaymentOrderStatus(orders, command.getStatus());
        incrementFailedCount(command.getOrderId());
        return true;
    }

    private PaymentEvent updatePaymentEventExtraDetails(PaymentStatusUpdateCommand command){
        Optional<PaymentEvent> paymentEventOptional = paymentEventRepository.findByOrderId(command.getOrderId());
        PaymentEvent event = paymentEventOptional.orElseThrow(() -> new EntityNotFoundException("결제 이벤트 없음"));

        PaymentExtraDetails details = command.getExtraDetails();
        event.setOrderName(details.getOrderName());
        event.setMethod(PaymentMethod.valueOf(details.getMethod().name()));
        event.setApprovedAt(details.getApprovedAt());
        event.setPaymentType(details.getType());
        event.setPspRawData(details.getPspRawData());
        event.setUpdatedAt(LocalDateTime.now());
        return event;
    }

    // AFTER_COMMIT 리스너는 트랜잭션이 끝난 뒤 실행되어 지연 로딩이 불가능하므로,
    // 엔티티 참조가 아닌 값이 모두 채워진 순수 데이터로 메시지를 구성해 여기서 넘긴다.
    // (buyer 이름은 AccountEntity의 지연 로딩 필드라 트랜잭션이 열려 있는 지금 시점에만 접근 가능하다)
    private PaymentEventMessage buildSuccessMessage(PaymentEvent event, List<PaymentOrder> orders) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", event.getOrderId());
        payload.put("paymentKey", event.getPaymentKey());
        payload.put("buyerId", event.getBuyerId());
        payload.put("buyerName", event.getAccountEntity().getUsername());
        payload.put("orderName", event.getOrderName());
        payload.put("totalAmount", event.totalAmount());
        payload.put("method", event.getMethod() != null ? event.getMethod().name() : null);
        payload.put("type", event.getPaymentType() != null ? event.getPaymentType().name() : null);
        payload.put("approvedAt", event.getApprovedAt());
        payload.put("items", orders.stream()
                .map(order -> {
                    // sellerId는 엔티티상 nullable이라 Map.of() 사용 시 NPE 위험이 있어 HashMap을 쓴다.
                    Map<String, Object> item = new HashMap<>();
                    item.put("sellerId", order.getSellerId());
                    item.put("productId", order.getProductId());
                    item.put("amount", order.getAmount());
                    item.put("quantity", order.getQuantity());
                    return item;
                })
                .collect(Collectors.toList()));

        return new PaymentEventMessage(PaymentEventMessageType.PAYMENT_CONFIRMATION_SUCCESS, payload, buildMetadata());
    }

    private PaymentEventMessage buildFailureMessage(PaymentEvent event, PaymentStatusUpdateCommand command) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", event.getOrderId());
        payload.put("paymentKey", command.getPaymentKey());
        payload.put("buyerId", event.getBuyerId());
        payload.put("buyerName", event.getAccountEntity().getUsername());
        payload.put("errorCode", command.getFailure().getErrorCode());
        payload.put("errorMessage", command.getFailure().getMessage());
        payload.put("failedAt", LocalDateTime.now());

        return new PaymentEventMessage(PaymentEventMessageType.PAYMENT_CONFIRMATION_FAILURE, payload, buildMetadata());
    }

    private Map<String, Object> buildMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "core-spa");
        metadata.put("occurredAt", LocalDateTime.now());
        metadata.put("schemaVersion", 1);
        return metadata;
    }

    private void incrementFailedCount(String orderId) {
        // orderId에 매칭되는 모든 payment_order 행을 한 번의 UPDATE로 +1 처리한다.
        // (과거: orders 개수만큼 루프를 돌아 다중 항목 주문에서 failed_count가 부풀려지던 버그)
        paymentOrderRepository.incrementFailedCountByOrderId(orderId);
    }

}

