package com.myboard.toy.controller.order.controller;

import com.myboard.toy.application.item.service.ItemService;
import com.myboard.toy.application.order.service.OrderService;
import com.myboard.toy.application.user.service.UserService;
import com.myboard.toy.domain.user.User;
import com.myboard.toy.infrastructure.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
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
    @GetMapping(value = "/orderForm")
    public String orderForm(){
        return "/order/orderForm";
    }

    @PostMapping(value = "/order")
    public String orderV1(
                        //@RequestParam("userId") Long userId,
                        @RequestParam("isbn") String  isbn,
                        @RequestParam("count") int count){

        User user = new User();
        user.setName("test계정");
        //Given - 로그인 로직 구현 전이므로 임시 User 하나 설정
        Long userId = userService.join(user);

        orderService.orderV2(userId,isbn,count);

        return "redirect:/search-books";
    }
}
