package com.myboard.toy.payment.adapter.in.web.view;

import com.myboard.toy.common.WebAdapter;
import com.myboard.toy.order.domain.dto.OrderDto;
import com.myboard.toy.order.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@WebAdapter
@RequiredArgsConstructor
@RequestMapping("/v2/toss")
public class CheckoutController {

    private final OrderService orderService;

    @GetMapping("")
    public String successPage(@RequestParam(value = "orderId", required = false) Long orderId,
                              Model model){

        OrderDto order = orderService.findOrderById(orderId);

        model.addAttribute("orderId",orderId);
        model.addAttribute("amount",order.getTotalAmount());
        return "toss/checkout";
    }
}
