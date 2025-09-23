package com.bookService.core.test.cart.mysql.service;

import com.bookService.core.domain.cart.repository.CartRepository;
import com.bookService.core.domain.cartitem.repository.CartItemRepository;
import com.bookService.core.domain.login.repository.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping
public class CartMySqlServiceTest {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AccountJpaRepository accountJpaRepository;


}
