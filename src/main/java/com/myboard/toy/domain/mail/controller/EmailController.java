package com.myboard.toy.domain.mail.controller;

import com.myboard.toy.domain.mail.dto.EmailDTO;
import com.myboard.toy.domain.mail.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@Controller
public class EmailController {

    private final EmailService email;

    @GetMapping("/inquire")
    public String inquireForm(){
        return "/academy/inquireForm";
    }

    @PostMapping("/sendMail")
    public String sendMail(EmailDTO msg){

        if(email.sendMailReject(msg)){
            System.out.println("Email 전송 시작!");
        }else System.out.println("실패");

        return "redirect:/inquire";
    }

}
