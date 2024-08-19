package com.myboard.toy.order.domain.entity;

import com.myboard.toy.order.domain.status.DeliveryStatus;
import com.myboard.toy.order.domain.status.OrderStatus;
import com.myboard.toy.sales.domain.entity.Cart;
import com.myboard.toy.security.domain.entity.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.*;


@Getter
@NoArgsConstructor
@Table(name = "orders")
@Entity
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    //주문 이름
    @Column(name = "order_name")
    private String orderName;

    //일 대 다
    @OneToMany(mappedBy = "order", cascade = ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    //주문 총 가격
    private int totalAmount;

    //주문시간
    private LocalDateTime orderDate;

    @OneToOne(cascade = ALL,fetch = LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    //주문 상태
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    //Builder용 생성자
    @Builder
    public Order(Account account, Delivery delivery, List<OrderItem> orderItems, OrderStatus status, LocalDateTime orderDate) {
        this.account = account;
        this.delivery = delivery;
        this.orderItems = orderItems;
        this.status = status;
        this.orderDate = orderDate;
        this.totalAmount = getTotalPrice(); // 주문 총액 계산
    }

    /* 연관관계 */
    public void setAccount(Account account){
        this.account = account;
        account.getOrders().add(this);
    }

    //주문 항목 추가
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
        this.totalAmount += orderItem.getTotalPrice();
    }

    //배송 정보 설정
    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //주문 상태 변경
    public void setStatus(OrderStatus status) {
        this.status = status;
    }


    /* 주문 취소 요청*/
    public void cancel(){
        if (this.delivery.getStatus() == DeliveryStatus.COMP){
            throw new IllegalStateException("이미 배송완료 된 상품은 취소가 불가능합니다.");
        }
        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems){
            orderItem.cancel();
        }
    }

    //총 주문 금액 계산
    private int calculateTotalAmount(){
        return this.orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }

    /* 주문 요청 메서드 - Cart를 사용함 */
    public static Order createOrderFromCart(Account account, Cart cart, Delivery delivery, List<OrderItem> orderItems) {

        Order order = Order.builder()
                .account(account)
                .delivery(delivery)
                .orderItems(orderItems)
                .status(OrderStatus.ORDER)
                .orderDate(LocalDateTime.now())
                .build();
        // orderItem 설정
        for (OrderItem orderItem : orderItems){
            orderItem.setOrder(order);
        }
        // 주문 총액 계산
        order.totalAmount = order.calculateTotalAmount();

        return order;
    }


    //==조회 로직==//
    /** 전체 주문 가격 조회 */
    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }
    /*
    public static class Builder {
        private Account account;
        private Delivery delivery;
        private List<OrderItem> orderItems = new ArrayList<>();
        private OrderStatus status;
        private LocalDateTime orderDate;

        public Builder account(Account account) {
            this.account = account;
            return this;
        }

        public Builder delivery(Delivery delivery) {
            this.delivery = delivery;
            return this;
        }

        public Builder orderItems(List<OrderItem> orderItems) {
            this.orderItems = orderItems;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder orderDate(LocalDateTime orderDate) {
            this.orderDate = orderDate;
            return this;
        }

        public Order build() {
            return new Order(account, delivery, orderItems, status, orderDate);
        }
    }
    */

}
