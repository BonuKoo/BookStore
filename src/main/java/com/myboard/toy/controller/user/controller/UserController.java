package com.myboard.toy.controller.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserController {

    @GetMapping("login")
    public String getLoginPage(){
        return "user/login";
    }
    @GetMapping("signUp")
    public String getSignUpPage(){
        return "user/signUp";
    }
}
