package com.bookService.core.domain.cart.repository;

import com.bookService.core.domain.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,Long>, CartRepository4QueryDsl {

    Optional<Cart> findByAccount_Id(Long accountId);

    // Cart와 CartItem을 한 번에 조회 (Lazy 초기화 방지)
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.id BETWEEN :startId AND :endId")
    List<Cart> findAllWithItemsByIdBetween(@Param("startId") Long startId, @Param("endId") Long endId);

}
