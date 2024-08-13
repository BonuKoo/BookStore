package com.myboard.toy.order.order.controller;

import com.myboard.toy.order.domain.Delivery;
import com.myboard.toy.order.domain.Order;
import com.myboard.toy.order.domain.status.DeliveryStatus;
import com.myboard.toy.sales.cart.service.CartService;
import com.myboard.toy.sales.domain.Cart;
import com.myboard.toy.sales.item.service.ItemService;
import com.myboard.toy.order.order.service.OrderService;
import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.eclipse.angus.mail.imap.protocol.MODSEQ;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final ItemService itemService;
    private final CartService cartService;

    @GetMapping(name = "/orderForm")
    public String showOrderForm(Model model,
                                Principal principal){
        AccountDto accountId = userService.getAccountIdByPrincipal(principal);

        Cart cart = cartService.getOrCreateCart(accountId);

        model.addAttribute("cartList", cart.getCartItems());
        //총 가격 계산
        model.addAttribute("cartTotalPrice",cart.getTotPrice());

        return "toss/checkout";

    }

    @PostMapping("/order/submit")
    public String submitOrder(
//            @RequestParam("address") String address,
//            @RequestParam("status") String status,
            Principal principal,
            Model model){

        AccountDto accountId = userService.getAccountIdByPrincipal(principal);

        Cart cart = cartService.getOrCreateCart(accountId);

        // 추후 webPage에서 address도 받을 수 있도록 변경

        Delivery delivery = Delivery.builder()
                .status(DeliveryStatus.READY)
                .build();
        //주문 생성 및 저장
        Order order = orderService.createOrderFromCart(accountId, cart, delivery);

        model.addAttribute("order",order);
        return "order/confirmation";
    }

}
