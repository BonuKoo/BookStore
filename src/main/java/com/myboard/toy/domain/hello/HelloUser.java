package com.myboard.toy.domain.hello;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor @AllArgsConstructor
public class HelloUser {

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
