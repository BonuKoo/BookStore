package com.myboard.toy.domain.mail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class EmailDTO {
    
    //수신자
    private String to;
    //발신자
    private String from;
    //제목
    private String subject;
    //내용
    private String text;


}


