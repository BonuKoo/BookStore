package com.myboard.toy.domain.orderitem;

import com.myboard.toy.domain.item.Item;
import com.myboard.toy.domain.order.Order;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
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

    private int orderPrice;             //주문 가격
    private int count;                  //주문 수량

    public void setOrder(Order order) {
        this.order = order;
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
