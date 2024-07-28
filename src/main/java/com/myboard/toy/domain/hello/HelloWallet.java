package com.myboard.toy.domain.hello;

import com.myboard.toy.domain.user.User;
import jakarta.persistence.*;

@Entity
public class HelloWallet {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY,orphanRemoval = true)
    private User user;

    //잔고
    private Integer balance;

}
