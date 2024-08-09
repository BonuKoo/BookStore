package com.myboard.toy.security.users.controller;

import com.myboard.toy.order.domain.Address;
import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public String signup(AccountDto dto){

        //주소
        Address address = Address.builder()
                .postcode(dto.getPostcode())
                .roadAddress(dto.getRoadAddress())
                .jibunAddress(dto.getJibunAddress())
                .detailAddress(dto.getDetailAddress())
                .extraAddress(dto.getExtraAddress())
                .build();

        Account account = Account.builder()
                .username(dto.getUsername())
                .nickname(dto.getNickname())
                .password(passwordEncoder.encode(dto.getPassword()))
                .age(dto.getAge())
                .address(address)
                .build();

        userService.createUser(account);

        return "redirect:/";
    }

    @GetMapping("/check-username")
    public String checkUsernamePage(Model model) {
        return "login/check-username";
    }
}

/*

    일단 check-username page를 띄우기까지는 성공

 */
