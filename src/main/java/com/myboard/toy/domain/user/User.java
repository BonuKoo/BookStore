package com.myboard.toy.domain.user;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;

import java.util.Set;

public class User {

    //PK
    private Long id;

    private String loginId;     //로그인 용 Id
    private String password;    //비밀번호
    private String name;        //이름
    private String role;        //시큐리티 용 권한
    private String birthDate;   //생년월일
    private int age;            //나이

//    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
//    private Set<ChatMessageEntity> chatMessages;

}
