package com.myboard.toy.web.home;

import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.users.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {

    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public String home(Principal principal, Model model) {
        if (principal != null) { // 인증된 사용자인 경우
            AccountDto userDetailsByPrincipal = userService.getUserDetailsByPrincipal(principal);
            String nickname = userDetailsByPrincipal.getNickname();
            model.addAttribute("nickname", nickname);
        } else { // 비로그인 사용자인 경우
            model.addAttribute("nickname", null); // 또는 필요에 따라 기본 메시지를 설정
        }
        return "home";
    }

}
