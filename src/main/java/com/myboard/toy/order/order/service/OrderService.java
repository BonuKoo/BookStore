package com.myboard.toy.order.order.service;

import com.myboard.toy.common.exception.ItemNotFoundException;
import com.myboard.toy.common.exception.OrderNotFoundException;
import com.myboard.toy.common.exception.UserNotFoundException;
import com.myboard.toy.order.domain.Delivery;
import com.myboard.toy.order.domain.status.DeliveryStatus;
import com.myboard.toy.sales.domain.Item;
import com.myboard.toy.sales.item.repository.ItemRepository;
import com.myboard.toy.order.domain.Order;
import com.myboard.toy.order.order.repository.OrderRepository;
import com.myboard.toy.order.domain.OrderItem;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    /*
    *   V2 = 유저 포함
     */

    public Long orderV2(Long accountId, String isbn, int count){

        //엔티티 조회
        Account account = userRepository.findById(accountId)
                .orElseThrow(UserNotFoundException::new);

        Item item = itemRepository.findByIsbn(isbn)
                .orElseThrow(ItemNotFoundException::new);

        //배송정보 생성
        Delivery delivery = new Delivery(account.getAddress(), DeliveryStatus.READY);

        //주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        //주문
        Order order = Order.createOrder(account,delivery,orderItem);

        //주문 저장
        orderRepository.save(order);
        return order.getId();
    }

    /* 주문 취소 */
    public void cancelOrder(Long orderId){
        //주문 엔티티 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);
        //주문 취소
        order.cancel();
    }

    /* 주문 검색
    public List<Order> findOrders(OrderSearch orderSearch){
        return orderRepository.findAll(orderSearch);
    }
    */

}
