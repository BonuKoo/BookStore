package com.myboard.toy.domain.hello.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/hello")
public class HelloControllerTest {

    @GetMapping("")
    public String hello(){
        return "/hello/hello";
    }
}

