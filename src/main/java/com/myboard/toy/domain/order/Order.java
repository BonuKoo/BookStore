package com.myboard.toy.domain.order;

import com.myboard.toy.domain.delivery.Delivery;
import com.myboard.toy.domain.delivery.DeliveryStatus;
import com.myboard.toy.domain.order.status.OrderStatus;
import com.myboard.toy.domain.orderitem.OrderItem;
import com.myboard.toy.securityproject.domain.entity.Account;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.*;

@Data
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
    
    //다 대 다
    @OneToMany(mappedBy = "order", cascade = ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    //주문 상태
    @OneToOne(cascade = ALL,fetch = LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; //주문시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status;


    // Builder용 생성자
    public Order(Account account, Delivery delivery, List<OrderItem> orderItems, OrderStatus status, LocalDateTime orderDate) {
        this.account = account;
        this.delivery = delivery;
        this.orderItems = orderItems;
        this.status = status;
        this.orderDate = orderDate;
    }


    /* 연관관계 */
    public void setAccount(Account account){
        this.account = account;
        account.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    /* 주문 요청 */
    public static Order createOrder(Account account, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order(account, delivery, Arrays.asList(orderItems), OrderStatus.ORDER, LocalDateTime.now());
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        return order;
    }

    /* 주문 취소 요청*/
    public void cancel(){
        if (delivery.getStatus() == DeliveryStatus.COMP){
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }
        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems){
            orderItem.cancel();
        }
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

}
