package com.myboard.toy.order.order.controller;

import com.myboard.toy.order.domain.Order;
import com.myboard.toy.order.domain.dto.OrderDto;
import com.myboard.toy.order.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class RestOrderController {

    private final OrderService orderService;

    @GetMapping("/total-amount")
    public ResponseEntity<Map<String,Object>> getTotalAmount(@RequestParam("orderId") Long orderId){
        // OrderService를 통해서 orderId로 주문을 조회

        OrderDto order = orderService.findOrderById(orderId);
        if (order == null){
            return ResponseEntity.notFound().build();
        }

        Map<String,Object> response = new HashMap<>();
        response.put("orderId",order.getOrderId());
        response.put("amount", order.getTotalAmount());
        return ResponseEntity.ok(response);
    }

}
