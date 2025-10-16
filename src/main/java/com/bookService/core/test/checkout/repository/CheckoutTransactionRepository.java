package com.bookService.core.test.checkout.repository;

import com.bookService.core.domain.payment.dto.PaymentCheckoutOptDtoForQueryProjection;
import com.bookService.core.domain.payment.entity.PaymentEvent;
import com.bookService.core.domain.payment.entity.QPaymentEvent;
import com.bookService.core.domain.payment.entity.QPaymentOrder;
import com.bookService.core.test.checkout.service.CheckoutFindExistingOrderServiceForDev;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@Repository
@Slf4j
@RequiredArgsConstructor
public class CheckoutTransactionRepository {
    private final CheckoutFindExistingOrderServiceForDev existingOrderService;

    private final PlatformTransactionManager txManager;

    private final EntityManager em;

    private QPaymentEvent paymentEvent = QPaymentEvent.paymentEvent;
    private QPaymentOrder paymentOrder = QPaymentOrder.paymentOrder;

    public Optional<PaymentCheckoutOptDtoForQueryProjection> createCheckoutOrSelectExistingPayment(PaymentEvent paymentEvent) {

        TransactionTemplate tx1 = new TransactionTemplate(txManager);
        tx1.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

        return tx1.execute(status -> {
            try {
                log.info("=================================진입 확인====================================");
                em.persist(paymentEvent);
                /**
                 * 직후 바로 예외 상황 발생 -> 상위 메서드로 프록시 우회 -> em.persist 로그 발생 X
                 * -> 아래의 catch까지 전파되지 않는 것 같다.
                 * */
                log.info("=============================em.persist======================================");
                em.flush();
                //tx1 성공

                return Optional.of(new PaymentCheckoutOptDtoForQueryProjection(
                        paymentEvent.getOrderId(), paymentEvent.getOrderName(), paymentEvent.totalAmount()));

            } catch (DataIntegrityViolationException e){

                log.warn("T1 Insert failed due to duplicate key for orderId: {}", paymentEvent.getOrderId());
                status.setRollbackOnly();
                return existingOrderService.findExistingOrder(paymentEvent.getOrderId());

            } catch (Exception e) {
                status.setRollbackOnly();
                throw e;
            }
        });
    }
}