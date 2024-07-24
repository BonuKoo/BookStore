package com.myboard.toy.application.mail.service;

import com.myboard.toy.domain.mail.dto.EmailDTO;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private final String FROM_MAIL = "zxcqew32@kakao.com";

    @Async
    public boolean sendMailReject(EmailDTO mail) {

        boolean msg = true;

        MimeMessage message = mailSender.createMimeMessage();

        //보내는 사람

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message,"UTF-8");
            message.setFrom(FROM_MAIL);

        //받는 사람
        helper.setFrom(FROM_MAIL);
        helper.setSubject(mail.getTitle());
        helper.setTo(mail.getTo());
        helper.setText(mail.getText(),true);

            System.out.println(mail.getText());

        mailSender.send(message);
        }catch (Exception e){
            e.printStackTrace();
            msg = false;
        }
        return msg;
    }

}
