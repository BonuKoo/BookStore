package com.myboard.toy.controller.order;

import com.myboard.toy.application.item.service.ItemService;
import com.myboard.toy.application.order.OrderService;
import com.myboard.toy.securityproject.users.repository.UserRepository;
import com.myboard.toy.securityproject.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final ItemService itemService;
    private final UserRepository userRepository;

    //주문 로직
    /*
    *   User getId가 존재한다는 가정 하에 실행
    * */

    // TODO
    @GetMapping("/orderForm")
    public String orderForm(@RequestParam(required = false) String title,
                            @RequestParam(required = false) String isbn,
                            @RequestParam(required = false) int discount,
                            Model model) {
        return "/order/orderForm";
    }

    @PostMapping(value = "/order")
    public String orderV1(
                        @RequestParam("userId") Long userId,
                        @RequestParam("isbn") String  isbn,
                        @RequestParam("count") int count){


        //TODO
        orderService.orderV2(userId,isbn,count);

        return "redirect:/search-books";
    }
}
