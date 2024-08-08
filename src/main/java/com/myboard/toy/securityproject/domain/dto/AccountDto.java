package com.myboard.toy.securityproject.domain.dto;

import com.myboard.toy.domain.address.Address;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private String id;
    private String username;
    private String nickname;
    private int age;
    private String password;
    private String passwordConfirm; // 비밀번호 확인

    //주소
    private String postcode;
    private String roadAddress;
    private String jibunAddress;
    private String detailAddress;
    private String extraAddress;

    private List<String> roles;
}
