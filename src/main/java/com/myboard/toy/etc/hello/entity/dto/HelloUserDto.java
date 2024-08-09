package com.myboard.toy.etc.hello.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class HelloUserDto {

    private Long id;

    private String loginId;
    // 비밀번호
    private String password;
    // 생년월일
    private LocalDate birthDate;
    // 이메일
    private String email;
    // 전화번호
    private String phoneNum;

}
