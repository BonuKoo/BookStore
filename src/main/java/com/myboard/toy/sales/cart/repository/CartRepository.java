package com.myboard.toy.sales.cart.repository;

import com.myboard.toy.sales.domain.Cart;
import com.myboard.toy.security.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,Long>,CartRepository4QueryDsl{

    Optional<Cart> findByAccount(Account account);

}
