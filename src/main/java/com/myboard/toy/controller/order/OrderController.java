package com.myboard.toy.controller.order;

import com.myboard.toy.application.item.service.ItemService;
import com.myboard.toy.application.order.OrderService;
import com.myboard.toy.securityproject.domain.dto.AccountDto;
import com.myboard.toy.securityproject.domain.entity.Account;
import com.myboard.toy.securityproject.users.repository.UserRepository;
import com.myboard.toy.securityproject.users.service.UserService;
import com.myboard.toy.securityproject.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
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
    private final AccountUtils accountUtils;

    // TODO
    @GetMapping("/orderForm")
    public String orderForm(
            Principal principal, Model model) {

        Account account = accountUtils.getAccountByPrincipal((UsernamePasswordAuthenticationToken) principal);


        return "/order/orderForm";
    }

    private Account getAccountByPrinciple(UsernamePasswordAuthenticationToken principal) {
        UsernamePasswordAuthenticationToken authenticationToken = principal;
        AccountDto accountDto = (AccountDto) authenticationToken.getPrincipal();
        String username1 = accountDto.getUsername();
        String username = username1;

        // username을 토대로 account 값을 db에서 조회한다.
        Account account = userService.getAccountByUsername(username);
        return account;
    }

}
