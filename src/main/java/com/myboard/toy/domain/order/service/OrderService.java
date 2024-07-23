package com.myboard.toy.domain.order.service;

import com.myboard.toy.common.exception.ItemNotFoundException;
import com.myboard.toy.common.exception.OrderNotFoundException;
import com.myboard.toy.common.exception.UserNotFoundException;
import com.myboard.toy.domain.delivery.Delivery;
import com.myboard.toy.domain.delivery.DeliveryStatus;
import com.myboard.toy.domain.item.Item;
import com.myboard.toy.domain.item.repository.ItemRepository;
import com.myboard.toy.domain.order.Order;
import com.myboard.toy.domain.order.repository.OrderRepository;
import com.myboard.toy.domain.orderitem.OrderItem;
import com.myboard.toy.domain.user.User;
import com.myboard.toy.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    /* 주문 */
    public Long order(Long userId, Long itemId, int count){

        //엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(ItemNotFoundException::new);

        //배송정보 생성
        Delivery delivery = new Delivery(user.getAddress(), DeliveryStatus.READY);

        //주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        //주문
        Order order = Order.createOrder(user,delivery,orderItem);

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
