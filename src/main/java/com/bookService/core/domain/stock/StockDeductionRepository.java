package com.bookService.core.domain.stock;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StockDeductionRepository extends JpaRepository<StockDeduction, Long> {

    boolean existsByOrderId(String orderId);
}
