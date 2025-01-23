package com.myboard.toy.sales.cart.service;

import com.myboard.toy.redis.repository.RedisCommon;
import com.myboard.toy.sales.cartitem.dto.RedisCartDto;
import com.myboard.toy.sales.cartitem.dto.RedisCartItemDto;
import com.myboard.toy.sales.domain.entity.Cart;
import com.myboard.toy.sales.domain.dto.CartDto;
import com.myboard.toy.sales.domain.dto.CartListDto;
import com.myboard.toy.sales.domain.dto.CartTotalPriceDto;
import com.myboard.toy.sales.cart.repository.CartRepository;
import com.myboard.toy.sales.domain.entity.CartItem;
import com.myboard.toy.sales.domain.entity.Item;
import com.myboard.toy.sales.item.repository.ItemRepository;
import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ItemRepository itemRepository; // Item 조회

    private final RedisCommon redisCommon;

    /* Find */
    public Cart findCartByAccountId(AccountDto dto){

        Long accountId = dto.getId();
        Optional<Account> accountOpt = userRepository.findById(accountId);
        Account account = accountOpt.orElseThrow();

        Optional<Cart> cart = cartRepository.findByAccount(account);

        if (cart.isEmpty()){
            Cart newCart = new Cart();
            newCart.createCart(account);
            return newCart;
        } else
            return cart.get();
    }

    /*
        * 수정 1
    */
    public Cart findCartByAccountId2(Long accountId){
        // Account와 연관된 Cart를 바로 조회
        return cartRepository.findByAccount_Id(accountId)
                .orElseGet(() -> {
                    Account account = userRepository.findById(accountId)
                            .orElseThrow(() -> new IllegalArgumentException("Invalid account ID"));
                    Cart newCart = Cart.builder()
                            .account(account)
                            .cartItems(new ArrayList<>())
                            .totPrice(0)
                            .build();
                    return cartRepository.save(newCart); // 저장 후 반환
                });
    }

    public CartDto getCartIdByAccountId(AccountDto dto){
        Long accountId = dto.getId();
        Optional<Account> accountOpt = userRepository.findById(accountId);
        Account account = accountOpt.orElseThrow();
        Long cardId = account.getCart().getId();
        return CartDto.builder()
                .id(cardId)
                .build();
    }

    //TODO
    // Cart -> CartDTO
    public Cart getOrCreateCart(AccountDto accountDto){
        Long accountId = accountDto.getId();
        Optional<Account> accountOpt = userRepository.findById(accountId);
        Account account = accountOpt.orElseThrow();

        Optional<Cart> cartExist = cartRepository.findByAccount(account);

        if (cartExist.isPresent()){

            return cartExist.get();

        }else {
            Cart newCart = new Cart();
            newCart.createCart(account);
            return cartRepository.save(newCart);
        }
    }

    public void save(Cart cart){
        cartRepository.save(cart);
    }


    public List<CartListDto> getCartList(AccountDto dto){

        Long accountId = dto.getId();
        Optional<Account> accountOpt = userRepository.findById(accountId);
        Account account = accountOpt.orElseThrow();

        Long cartId = account.getCart().getId();
        return cartRepository.getCartList(cartId);
    };

    public List<CartListDto> getCartListV2(String cardId){
        Long accountId = Long.parseLong(cardId);
        return cartRepository.getCartList(accountId);
    };

    public CartTotalPriceDto getCartTotalPrice(AccountDto dto){

        Long accountId = dto.getId();
        Optional<Account> accountOpt = userRepository.findById(accountId);
        Account account = accountOpt.orElseThrow();


        Long cartId = account.getCart().getId();

        List<CartListDto> cartList = cartRepository.getCartList(cartId);

        int extractdTotalPrice = cartList.stream()
                .mapToInt(CartListDto::getTotPrice)
                .sum();
        CartTotalPriceDto totalPrice = new CartTotalPriceDto(extractdTotalPrice);
        return totalPrice;
    };


    /**
     * Redis -> DB
     */

    @Transactional
    public void saveCartToDatabase(String cartId, RedisCartDto redisCart) {

        long cartIdToLong = Long.parseLong(cartId);

        // Cart 조회
        Cart cart = cartRepository.findById(cartIdToLong)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

        // Redis의 itemId 수집
        List<String> redisItemIds = redisCart.getItems().values()
                .stream()
                .map(RedisCartItemDto::getItemId)
                .toList();


        // 기존 CartItem 중 Redis에 없는 항목 삭제
        cart.getCartItems()
                .removeIf(cartItem -> !redisItemIds.contains(cartItem
                .getItem()
                .getIsbn()
        ));

        // Redis의 Item 정보 조회
        Map<String, Item> itemsMap = itemRepository.findAllById(redisItemIds).stream()
                .collect(Collectors.toMap(Item::getIsbn, Function.identity()));

        // RedisCartDto의 items를 CartItem으로 변환
        for (RedisCartItemDto redisCartItem : redisCart.getItems().values()) {
            Item item = itemsMap.get(redisCartItem.getItemId());
            if (item == null) {
                throw new IllegalArgumentException("Item not found: " + redisCartItem.getItemId());
            }

            // 기존 항목 업데이트 또는 새 항목 추가
            Optional<CartItem> existingItem = cart.getCartItems().stream()
                    .filter(ci -> ci.getItem()
                            .getIsbn()
                            .equals(redisCartItem.getItemId())
                    )
                    .findFirst();

            if (existingItem.isPresent()) {

                existingItem.get().setCount(redisCartItem.getCount());

            } else {
                CartItem cartItem = CartItem.builder()
                        .item(item)
                        .cart(cart)
                        .count(redisCartItem.getCount())
                        .build();
                cart.addCartItem(cartItem);
            }
        }

        // Cart 저장
        cartRepository.save(cart);

        log.info("Cart for cartId {} saved to database.", cartId);
    }


    public void syncCartOnExpiration(String userId) {
        // Redis에 데이터가 남아 있지 않더라도 DB에 최신 데이터를 저장
        RedisCartDto cart = redisCommon.getFromHash("cart:" + userId, "cart", RedisCartDto.class);

        if (cart == null) {
            log.warn("Cart data not found for userId: {}. It might be already expired.", userId);
            return;
        }

        // 데이터베이스 저장 로직
        saveCartToDatabase(userId, cart);
    }

}
