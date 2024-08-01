package com.myboard.toy.securityproject.users.controller;

import com.myboard.toy.securityproject.domain.dto.AccountDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping(value="/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "exception", required = false) String exception, Model model){
        model.addAttribute("error",error);
        model.addAttribute("exception",exception);
        return "login/login";
    }

    @GetMapping(value="/api/login")
    public String restLogin(){
        return "rest/login";
    }


    @GetMapping("/signup")
    public String signup(){
        return "login/signup";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response){
        Authentication authentication = SecurityContextHolder.getContextHolderStrategy().getContext().getAuthentication();;
        if (authentication != null){
            new SecurityContextLogoutHandler().logout(request,response,authentication);
        }
            return "redirect:/login";

    }

    @GetMapping("/denied")
    public String accessDenied(@RequestParam(value = "exception",required = false)String exception,
                               @AuthenticationPrincipal AccountDto accountDto, Model model){
        model.addAttribute("username",accountDto.getUsername());
        model.addAttribute("exception",exception);

        return "login/denied";
    }
}
/*
* AccountDto가 현재 Principal 객체이다.
* -> FormAuthticationProvider를 확인해보면 나온다.
*
*
*     return new UsernamePasswordAuthenticationToken(accountContext.getAccountDto(),null,accountContext.getAuthorities());
      인증 객체인 AuthenticationToken에 AccountDto를 받고 있다.
 * */