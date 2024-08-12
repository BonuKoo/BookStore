package com.myboard.toy.order.order.controller;

import com.myboard.toy.sales.item.service.ItemService;
import com.myboard.toy.order.order.service.OrderService;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final ItemService itemService;

    // TODO
    /*
    @GetMapping("/orderForm")
    public String orderForm(
            Principal principal, Model model) {

        Account account = accountUtils.getAccountByPrincipal((UsernamePasswordAuthenticationToken) principal);

        return "/order/orderForm";
    }
    */
}
