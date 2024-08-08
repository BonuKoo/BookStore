package com.myboard.toy.securityproject.users.controller;

import com.myboard.toy.domain.address.Address;
import com.myboard.toy.securityproject.domain.dto.AccountDto;
import com.myboard.toy.securityproject.domain.entity.Account;
import com.myboard.toy.securityproject.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
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
}
