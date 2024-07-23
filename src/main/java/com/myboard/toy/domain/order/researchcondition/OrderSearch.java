package com.myboard.toy.domain.order.researchcondition;

import com.myboard.toy.domain.order.status.OrderStatus;
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
