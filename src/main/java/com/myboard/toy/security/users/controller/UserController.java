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
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

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
    
    @GetMapping("/profile")
    public String getProfile(Model model,
                             Principal principal){
        AccountDto account = userService.getUserDetailsByPrincipal(principal);

        model.addAttribute("account", account);
        return "user/profile";
    }

    @GetMapping("/edit-profile")
    public String editProfilePage(Model model, Principal principal) {
        AccountDto account = userService.getUserDetailsByPrincipal(principal);

        model.addAttribute("account", account);
        return "user/edit-profile";
    }

    @PostMapping("/update-profile")
    @ResponseBody
    public Map<String, Object> updateProfile(AccountDto dto, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.updateAccount(dto, principal);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }

}

