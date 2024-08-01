package com.myboard.toy.infrastructure.cart;

import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.securityproject.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,Long> {

    Optional<Cart> findByAccount(Account account);

}
