package com.myboard.toy.domain.hello.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
//@Controller
//@RequestMapping("/hello")
@RequiredArgsConstructor
public class HelloControllerTest {
    /*
    private final HelloRepositoryTest helloRepository;
    private final FileStore fileStore;
    */

    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

}

