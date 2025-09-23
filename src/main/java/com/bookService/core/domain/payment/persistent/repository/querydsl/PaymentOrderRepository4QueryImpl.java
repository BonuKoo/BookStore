package com.bookService.core.domain.payment.persistent.repository.querydsl;

import com.bookService.core.common.exception.PaymentValidationException;
import com.bookService.core.domain.payment.entity.PaymentOrder;
import com.bookService.core.domain.payment.entity.QPaymentOrder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class PaymentOrderRepository4QueryImpl implements PaymentOrderRepository4Query {

    private final JPAQueryFactory queryFactory;

    QPaymentOrder paymentOrder = new QPaymentOrder(QPaymentOrder.paymentOrder);

    public PaymentOrderRepository4QueryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    // todo 추후 DTO 반환으로 ID와 STATUS만 가져오도록 수정 할 수 있다.
    public List<PaymentOrder> findListPaymentOrderByIdempotencyKey(String orderId){
        List<PaymentOrder> query = queryFactory
                .selectFrom(paymentOrder)
                .where(paymentOrder.orderId.eq(orderId))
                .fetch();
        return query;
    };

    public boolean isValid(String orderId, long amount){
        Long totalAmount = Long.valueOf(queryFactory
                .select(paymentOrder.amount.sum())
                .from(paymentOrder)
                .where(paymentOrder.orderId.eq(orderId))
                .fetchOne());

        if (totalAmount == null) {
            throw new PaymentValidationException("해당 orderId에 대한 결제 내역이 없습니다: " + orderId);
        }

        if (totalAmount == amount) {
            return true;
        } else {
            throw new PaymentValidationException(
                    String.format("결제 (orderId: %s) 에서 금액 (amount: %d)이 올바르지 않습니다. (DB 총합: %d)", orderId, amount, totalAmount)
            );
        }
    }

    @Transactional
    public long incrementFailedCountByOrderId(String orderId){
        return queryFactory.update(paymentOrder)
                .set(paymentOrder.failed_count, paymentOrder.failed_count.add(1))
                .where(paymentOrder.orderId.eq(orderId))
                .execute();
    }

}