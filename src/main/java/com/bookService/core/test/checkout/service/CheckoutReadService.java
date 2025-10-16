package com.bookService.core.test.checkout.service;

import com.bookService.core.domain.cartitem.dto.CheckoutItemForQueryProjection2;
import com.bookService.core.domain.cartitem.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutReadService {

    private final CartItemRepository cartItemRepository;

    @Transactional(readOnly = true)
    public List<CheckoutItemForQueryProjection2> getCartItemsForEventCreation(List<Long> cartItemIds) {
        return cartItemRepository.customCartItemProjection2(cartItemIds);
    }

}
