package com.bookService.core.domain.payment.persistent;

import com.bookService.core.common.exception.checkout.PaymentAlreadyProcessedException;
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
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.bookService.core.domain.payment.enumtype.PaymentStatus.*;


@Repository
@RequiredArgsConstructor
public class PaymentStatusUpdateRepository {

    private final PaymentEventRepository paymentEventRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentOrderHistoryRepository paymentOrderHistoryRepository;

    //private final PaymentOutboxService paymentOutboxService;

    //private final PaymentEventMessagePublisher messagePublisher;

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

    @Transactional
    private boolean updatePaymentStatusToSuccess(PaymentStatusUpdateCommand command){
        List<PaymentOrder> orders = paymentOrderRepository.findListPaymentOrderByIdempotencyKey(command.getOrderId());
        insertPaymentHistory(orders, command.getStatus(),"PAYMENT_CONFIRMATION_DONE");
        updatePaymentOrderStatus(orders, command.getStatus());
        updatePaymentEventExtraDetails(command);

        /*
        메시지 이벤트 TODO
        // Outbox 저장
        PaymentEventMessage message = paymentOutboxService.insertOutbox(command);
        // 이벤트 발생
        messagePublisher.publish(message);
        */
        return true;
    }

    private boolean updatePaymentStatusToFailure(PaymentStatusUpdateCommand command){
        List<PaymentOrder> orders = paymentOrderRepository.findListPaymentOrderByIdempotencyKey(command.getOrderId());
        insertPaymentHistory(orders, command.getStatus(), command.getFailure().toString());
        updatePaymentOrderStatus(orders, command.getStatus());
        return true;
    }
    private  boolean updatePaymentStatusToUnknown(PaymentStatusUpdateCommand command){
        List<PaymentOrder> orders = paymentOrderRepository.findListPaymentOrderByIdempotencyKey(command.getOrderId());
        insertPaymentHistory(orders, command.getStatus(), command.getFailure().toString());
        updatePaymentOrderStatus(orders, command.getStatus());
        incrementFailedCount(command.getOrderId());
        return true;
    }

    private void updatePaymentEventExtraDetails(PaymentStatusUpdateCommand command){
        Optional<PaymentEvent> paymentEventOptional = paymentEventRepository.findByOrderId(command.getOrderId());
        PaymentEvent event = paymentEventOptional.orElseThrow(() -> new EntityNotFoundException("결제 이벤트 없음"));

        PaymentExtraDetails details = command.getExtraDetails();
        event.setOrderName(details.getOrderName());
        event.setMethod(PaymentMethod.valueOf(details.getMethod().name()));
        event.setApprovedAt(details.getApprovedAt());
        event.setPaymentType(details.getType());
        event.setPspRawData(details.getPspRawData());
        event.setUpdatedAt(LocalDateTime.now());
    }

    private void incrementFailedCount(String orderId) {
        List<PaymentOrder> orders = paymentOrderRepository.findListPaymentOrderByIdempotencyKey(orderId);
        for (PaymentOrder order : orders){
            paymentOrderRepository.incrementFailedCountByOrderId(orderId);
        }
    }

}

