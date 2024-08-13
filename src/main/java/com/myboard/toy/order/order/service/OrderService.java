package com.myboard.toy.order.order.service;

import com.myboard.toy.common.exception.ItemNotFoundException;
import com.myboard.toy.common.exception.OrderNotFoundException;
import com.myboard.toy.common.exception.UserNotFoundException;
import com.myboard.toy.order.domain.Delivery;
import com.myboard.toy.order.domain.status.DeliveryStatus;
import com.myboard.toy.sales.domain.Cart;
import com.myboard.toy.sales.domain.Item;
import com.myboard.toy.sales.item.repository.ItemRepository;
import com.myboard.toy.order.domain.Order;
import com.myboard.toy.order.order.repository.OrderRepository;
import com.myboard.toy.order.domain.OrderItem;
import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    /*
    *   V2 = 유저 포함
     */

    @Transactional
    public Order createOrderFromCart(AccountDto accountDto, Cart cart, Delivery delivery){

        Long accountId = accountDto.getId();
        Optional<Account> accountOpt = userRepository.findById(accountId);
        Account account = accountOpt.orElseThrow();

        //장바구니에서 주문 항목 생성
        List<OrderItem> orderItems = cart.getCartItems().stream()
                .map(cartItem -> {
                    //CartItem을 OrderItem으로 변환

                    Item item = cartItem.getItem();
                    int count = cartItem.getCount();

                    //재고 감소
                    item.removeStock(count);

                    //OrderItem 생성
                    OrderItem orderItem = OrderItem.createOrderItem(item, count);
                    return orderItem;
                })
                .toList();

        //계정의 주소를 우선 받도록 설정 TODO 추후 웹에서 받도록 수정
        delivery.setAddress(account.getAddress());

        //Order 생성
        Order order = Order.createOrderFromCart(account, cart, delivery, orderItems);

        //Order 저장
        orderRepository.save(order);

        //장바구니 비우기
        cart.getCartItems().clear();
        return order;
    }

}
