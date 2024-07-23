package com.myboard.toy.domain.order;

import com.myboard.toy.domain.delivery.Delivery;
import com.myboard.toy.domain.orderitem.OrderItem;
import com.myboard.toy.domain.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.FetchType.*;

@Data
@Table(name = "orders")
@Entity
public class Order {

    @Column(name = "order_id")
    @Id @GeneratedValue
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = LAZY)
    private User user;
    
    //다 대 다
    @OneToMany(mappedBy = "order", cascade = ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    //주문 상태
    @OneToOne(cascade = ALL,fetch = LAZY)
    private Delivery delivery;

    private LocalDateTime orderDate; //주문시간




    /* 연관관계 */
    public void setUser(User user){
        this.user = user;
        user.getOrders().add(this);
    }
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }
}
