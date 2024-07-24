package com.myboard.toy.controller.mail.controller;

import com.myboard.toy.application.hello.service.HelloUserService;
import com.myboard.toy.domain.mail.dto.EmailDTO;
import com.myboard.toy.application.mail.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class EmailController {

    @Autowired
    private HelloUserService userService;

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


    // 비밀번호 재설정 폼
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email, @RequestParam String newPassword, Model model) {
        userService.resetPassword(email, newPassword);
        model.addAttribute("message", "Password reset successfully.");
        return "resetPasswordResult"; // 결과 페이지로 리디렉션
    }

}
/*



 */