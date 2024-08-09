package com.myboard.toy.order.domain;

import com.myboard.toy.sales.domain.Item;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@Table(name = "order_item")
@Entity
public class OrderItem {

    @Column(name = "order_item_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;                  //주문 상품
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice;             //주문 가격
    private int count;                  //주문 수량

    public void setOrder(Order order) {
        this.order = order;
    }

    @Builder
    public OrderItem(Item item, int orderPrice, int count) {
        this.item = item;
        this.orderPrice = orderPrice;
        this.count = count;
    }

    /* 생성 메서드 */
    public static OrderItem createOrderItem(Item item,int orderPrice,int count){
        OrderItem orderItem = OrderItem.builder()
                .item(item)
                .orderPrice(orderPrice)
                .count(count)
                .build();
        item.removeStock(count);
        return orderItem;
    }
    /* 주문 취소 */
    public void cancel(){
        getItem().addStock(count);
    }
    /* 조회 */
    /* 주문 전체 가격 조회 */
    public int getTotalPrice(){
        return getOrderPrice() * getCount();
    }
}
