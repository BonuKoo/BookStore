package com.myboard.toy.controller.hello;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/hello")
@Controller
@Slf4j
public class HelloControllerForOrder {



    @GetMapping("/")
    public String home(){
        log.info("Controller For Test");
        return "hello/home";
    }


}
