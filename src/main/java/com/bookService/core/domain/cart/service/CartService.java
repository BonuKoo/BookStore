package com.bookService.core.domain.cart.service;

import com.bookService.core.domain.cart.Cart;
import com.bookService.core.domain.cart.repository.CartRepository;
import com.bookService.core.domain.cartitem.dto.CartListDTOForQueryProjection;
import com.bookService.core.domain.cartitem.dto.CartTotalPriceDto;
import com.bookService.core.domain.login.dto.AccountDTO;
import com.bookService.core.domain.login.entity.AccountEntity;
import com.bookService.core.domain.login.repository.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final AccountJpaRepository userRepository;
    private final CartRepository cartRepository;

    /* Find */
    /*
     * 수정 1
     */
    public Cart findCartByAccountIdLongType(Long accountId){
        // Account와 연관된 Cart를 바로 조회
        return cartRepository.findByAccount_Id(accountId)
                .orElseGet(() -> {
                    AccountEntity account = userRepository.findById(accountId)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid account ID"));
                    Cart newCart = new Cart(account, new ArrayList<>(), 0);

                    return cartRepository.save(newCart); // 저장 후 반환
                });
    }

    public Long findCartByAccountIdStringType(String userId){
        long userIdForFindCart = Long.parseLong(userId);
        Optional<Cart> findCartByAccountOpt = cartRepository.findByAccount_Id(userIdForFindCart);
                //.orElseThrow(() -> new IllegalArgumentException("장바구니 없음"));

        Cart cart = findCartByAccountOpt.orElseThrow();
        Long cartId = cart.getId();
        return cartId;
    }

    public void save(Cart cart){
        cartRepository.save(cart);
    }

    public List<CartListDTOForQueryProjection> getCartItemList(String userId){

        Long accountId = Long.parseLong(userId);
        Optional<AccountEntity> accountOpt = userRepository.findById(accountId);
        AccountEntity account = accountOpt.orElseThrow();

        Long cartId = account.getCart().getId();
        return cartRepository.getCartList(cartId);
    };

    public List<CartListDTOForQueryProjection> getCartListV2(String cartId){
        Long accountId = Long.parseLong(cartId);
        return cartRepository.getCartList(accountId);
    };

    public CartTotalPriceDto getCartTotalPrice(AccountDTO dto){

        Long accountId = dto.getId();
        Optional<AccountEntity> accountOpt = userRepository.findById(accountId);
        AccountEntity account = accountOpt.orElseThrow();


        Long cartId = account.getCart().getId();

        List<CartListDTOForQueryProjection> cartList = cartRepository.getCartList(cartId);

        int extractdTotalPrice = cartList.stream()
                .mapToInt(CartListDTOForQueryProjection::getTotPrice)
                .sum();
        CartTotalPriceDto totalPrice = new CartTotalPriceDto(extractdTotalPrice);
        return totalPrice;
    };

}
