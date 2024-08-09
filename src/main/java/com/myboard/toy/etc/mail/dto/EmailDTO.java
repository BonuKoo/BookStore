package com.myboard.toy.etc.mail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class EmailDTO {

    private String title;
    private String to;
    private String text;
    private String email;
    private String name;
    private String content;
    private String phoneNum;
}


