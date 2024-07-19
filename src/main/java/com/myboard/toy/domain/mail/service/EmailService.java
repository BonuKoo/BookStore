package com.myboard.toy.domain.mail.service;

import com.myboard.toy.domain.mail.dto.EmailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private final String FROM_MAIL = "zxcqew32@naver.com";

    @Async
    public boolean sendMailReject(EmailDTO mail) {
        boolean msg = true;
        SimpleMailMessage message = new SimpleMailMessage();
        
        //보낼 사람
        message.setFrom(FROM_MAIL);
        //받는 사람
        message.setTo(mail.getFrom());
        message.setSubject(mail.getSubject());
        message.setText(mail.getText());

        try {
            mailSender.send(message);
        }catch (Exception e){
            e.printStackTrace();
            msg = false;
        }
//        log.info("받는 사람, 제목, 내용 : {},{},{}",to,subject,text);
        mailSender.send(message);
        return msg;
    }

}
