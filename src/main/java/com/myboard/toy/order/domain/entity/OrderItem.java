package com.myboard.toy.order.domain.entity;

import com.myboard.toy.sales.domain.entity.Item;
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
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn")
    private Item item;                  //주문 상품

    private int count;                  //주문한 상품 개수

    private int orderPrice;             //상품 가격


    // ==생성 메서드 ==//
    public static OrderItem createOrderItem(Item item, int count){

        OrderItem orderItem = OrderItem.builder()
                .item(item)
                .count(count)
                .orderPrice(item.getPrice())
                .build();

        //주문을 만들면 실시간으로 item 수량이 감소해야 하니까.
//        item.removeStock(count);
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

    // == private setters == //
    private void setItem(Item item) {
        this.item = item;
    }

    private void setCount(int count) {
        this.count = count;
    }

    private void setOrderPrice(int orderPrice) {
        this.orderPrice = orderPrice;
    }

    void setOrder(Order order) {
        this.order = order;
    }

    @Builder
    public OrderItem(Item item, int orderPrice, int count, Order order) {
        this.item = item;
        this.orderPrice = orderPrice;
        this.count = count;
        this.order = order;
    }
}
