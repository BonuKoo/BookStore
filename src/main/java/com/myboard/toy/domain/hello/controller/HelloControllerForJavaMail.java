package com.myboard.toy.domain.hello.controller;

import com.myboard.toy.domain.hello.HelloUser;
import com.myboard.toy.domain.hello.service.HelloUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/hello")
public class HelloControllerForJavaMail {

    @Autowired
    private HelloUserService userService;
    @Autowired
    private JavaMailSender mailSender;


    @GetMapping("")
    public String hello(){
        return "/hello/hello";
    }
    
    @PostMapping("/resetPassword")
    public String requestTemporaryPassword(@RequestParam Long id,
                                           @RequestParam String email,
                                           Model model){

        HelloUser user = userService.getUserById(id);

        if(user != null && user.getEmail().equals(email)){
            String temporaryPassword = generateTemporaryPassword();

            // Update the user with the new temporary password
            userService.resetPassword(email, temporaryPassword);

            // Send the temporary password via email
            sendTemporaryPasswordEmail(email, temporaryPassword);
            model.addAttribute("message", "임시 비밀번호가 발송됐습니다..");
        } else {
            model.addAttribute("message", "Invalid ID or email.");
        }
        return "resetPasswordRequest";

    }
    //이메일 보냄
    private void sendTemporaryPasswordEmail(String email, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your Temporary Password");
        message.setText("Your temporary password is: " + temporaryPassword);
        mailSender.send(message);
    }
    //임시 비밀번호 생성
    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString();
    }

}

