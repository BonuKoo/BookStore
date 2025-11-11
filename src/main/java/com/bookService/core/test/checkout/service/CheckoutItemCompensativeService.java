package com.bookService.core.test.checkout.service;

import com.bookService.core.common.exception.checkout.BusinessLogicException;
import com.bookService.core.common.exception.checkout.CheckoutErrorCode;
import com.bookService.core.domain.item.service.ItemService;
import com.bookService.core.domain.payment.entity.PaymentOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//@Service
//@RequiredArgsConstructor
//@Slf4j
public class CheckoutItemCompensativeService {

//    private final ItemService itemService;
    /*
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increaseQuantity(List<PaymentOrder> paymentOrders){

        try {
            paymentOrders.forEach(
                    paymentOrder -> {
                        // 다시 paymentOrder 만큼 다시 재고를 올린다.
                        itemService.increase(paymentOrder.getProductId(),paymentOrder.getAmount());
                    }
            );
        } catch (Exception e){
            throw new BusinessLogicException(CheckoutErrorCode.COMPENSATION_FAILURE,e);
        }

    }*/
}
