package com.myboard.toy.domain.orderitem;

import com.myboard.toy.domain.item.Item;
import com.myboard.toy.domain.order.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Table(name = "order_item")
@Entity
public class OrderItem {

    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;                  //주문 상품
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;



    public void setOrder(Order order) {
        this.order = order;
    }

    private int orderPrice;             //주문 가격
    private int count;                  //주문 수량
}
