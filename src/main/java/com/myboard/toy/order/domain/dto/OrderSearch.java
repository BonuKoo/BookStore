package com.myboard.toy.order.domain.dto;

import com.myboard.toy.order.domain.status.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor @AllArgsConstructor
public class OrderSearch {

    //회원 이름
    private String userName;
    //주문 상태 [ORDER, CANCEL]
    private OrderStatus orderStatus;


}
