package com.bookService.core.domain.payment.persistent.repository.querydsl;

import com.bookService.core.domain.payment.dto.PaymentCheckoutOptDtoForQueryProjection;
import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import com.bookService.core.domain.payment.dto.PaymentEventDto;
import com.bookService.core.domain.payment.dto.PaymentOrderDto;
import com.bookService.core.domain.payment.dto.PendingPaymentRowDto;
import com.bookService.core.domain.payment.entity.QPaymentEvent;
import com.bookService.core.domain.payment.entity.QPaymentOrder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentEventRepository4QueryImpl implements PaymentEventRepository4Query{

    private final JPAQueryFactory queryFactory;
    private QPaymentEvent paymentEvent = QPaymentEvent.paymentEvent;
    private QPaymentOrder paymentOrder = QPaymentOrder.paymentOrder;

    public PaymentEventRepository4QueryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<PendingPaymentRowDto> findPendingPaymentRows(LocalDateTime now) {

        LocalDateTime thresholdTime = now.minusMinutes(3);

        return queryFactory
                .select(Projections.constructor(PendingPaymentRowDto.class,
                        paymentEvent.id,
                        paymentEvent.paymentKey,
                        paymentEvent.orderId,
                        paymentOrder.id,
                        paymentOrder.paymentStatus,
                        paymentOrder.amount,
                        paymentOrder.failed_count,
                        paymentOrder.threshold
                ))
                .from(paymentEvent)
                .join(paymentOrder).on(paymentOrder.paymentEvent.eq(paymentEvent))
                .where(
                        paymentOrder.failed_count.lt(paymentOrder.threshold),
                        paymentOrder.paymentStatus.eq(PaymentStatus.UNKNOWN)
                                .or(paymentOrder.paymentStatus.eq(PaymentStatus.EXECUTING)
                                        .and(paymentOrder.updatedAt.loe(thresholdTime)))
                )
                .limit(10)
                .fetch();
    }

    @Override
    public PaymentEventDto getPaymentEventAndOrders(String orderId) {
        PaymentEventDto paymentEventDto = queryFactory
                .select(Projections.constructor(PaymentEventDto.class,
                        paymentEvent.id,
                        paymentEvent.orderId,
                        paymentEvent.orderName,
                        paymentEvent.buyerId,
                        paymentEvent.isPaymentDone
                ))
                .from(paymentEvent)
                .where(paymentEvent.orderId.eq(orderId))
                .fetchOne();
        List<PaymentOrderDto> orders = queryFactory
                .select(Projections.constructor(PaymentOrderDto.class,
                        paymentOrder.id,
                        paymentOrder.sellerId,
                        paymentOrder.productId,
                        paymentOrder.orderId,
                        paymentOrder.paymentStatus,
                        paymentOrder.amount,
                        paymentOrder.isLedgerUpdated,
                        paymentOrder.isWalletUpdated
                ))
                .from(paymentOrder)
                .where(paymentOrder.orderId.eq(orderId))
                .fetch();

        paymentEventDto.setPaymentOrders(orders);

        return paymentEventDto;
    }
    @Override
    public void handlePaymentCompletion(PaymentEventDto paymentEventDto) {
        /*
        handleLedgerUpdate(paymentEventDto);
        handleWalletUpdate(paymentEventDto);
         */
        queryFactory.update(paymentEvent)
                .set(paymentEvent.isPaymentDone, true)
                .where(paymentEvent.id.eq(paymentEventDto.getId()))
                .execute();
    }

    @Override
    public Optional<PaymentCheckoutOptDtoForQueryProjection> findPaymentOptByOrderID(String orderId) {
        PaymentCheckoutOptDtoForQueryProjection projectionOpt = queryFactory
                .select(Projections.constructor(PaymentCheckoutOptDtoForQueryProjection.class,
                        paymentEvent.orderId,
                        paymentEvent.orderName,
                        paymentOrder.amount.sum().castToNum(Long.class)
                        ))
                .from(paymentEvent)
                .join(paymentEvent.paymentOrders,paymentOrder)
                .where(paymentEvent.orderId.eq(orderId))
                .groupBy(paymentEvent.orderId, paymentEvent.orderName)
                .fetchOne();

        return Optional.ofNullable(projectionOpt);
    }
    /*

    @Override
    public void handleWalletUpdate(PaymentEventDto paymentEventDto) {



        List<Long> paymentOrderIds = paymentEventDto.getPaymentOrders().stream()
                .map(PaymentOrderDto::getId)
                .toList();
        if(!paymentOrderIds.isEmpty()){
            queryFactory.update(paymentOrder)
                    .set(paymentOrder.isWalletUpdated, true)
                    .where(paymentOrder.id.in(paymentOrderIds))
                    .execute();
        }
    }

    @Override
    public void handleLedgerUpdate(PaymentEventDto paymentEventDto) {

        List<Long> paymentOrderIds = paymentEventDto.getPaymentOrders().stream()
                .map(PaymentOrderDto::getId)
                .toList();
        if(!paymentOrderIds.isEmpty()){
            queryFactory.update(paymentOrder)
                    .set(paymentOrder.isLedgerUpdated, true)
                    .where(paymentOrder.id.in(paymentOrderIds))
                    .execute();
        }
    }
    */
}
