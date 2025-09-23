package com.bookService.core.domain.payment.persistent.repository.jpa;

import com.bookService.core.domain.payment.entity.PaymentOrderHistory;
import com.bookService.core.domain.payment.persistent.repository.PaymentOrderHistoryRepository;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentOrderHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaPaymentOrderHistoryRepository implements PaymentOrderHistoryRepository {
    private final SpringDataJpaPaymentOrderHistoryRepository springDataJpaPaymentOrderHistoryRepository;

    public void saveAll(List<PaymentOrderHistory> histories){
        springDataJpaPaymentOrderHistoryRepository.saveAll(histories);
    }

}
