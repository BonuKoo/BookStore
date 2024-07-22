package com.myboard.toy.domain.message;

import com.myboard.toy.domain.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

//@Entity
public class ChatMessageEntity {

    //@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@ManyToOne
    //@JoinColumn(name = "user_id",nullable = false)
    private User user;

    //@Column(nullable = false)
    private String content;

    //@Column(nullable = false)
    private LocalDateTime timestamp;



}
