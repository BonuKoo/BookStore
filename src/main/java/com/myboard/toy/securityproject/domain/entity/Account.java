package com.myboard.toy.securityproject.domain.entity;

import com.myboard.toy.domain.address.Address;
import com.myboard.toy.domain.board.Board;
import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.domain.order.Order;
import com.myboard.toy.domain.reply.Reply;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Account implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username",unique = true)
    private String username;

    @Column
    private String nickname;

    @Column
    private int age;

    @Column
    private String password;

    @Embedded
    private Address address;

    //게시판
    @Builder.Default
    @OneToMany(mappedBy = "account",cascade = CascadeType.ALL,orphanRemoval = true)
    List<Board> boards = new ArrayList<>();

    //댓글
    @Builder.Default
    @OneToMany(mappedBy = "account",cascade = CascadeType.ALL,orphanRemoval = true)
    List<Reply> replys = new ArrayList<>();

    //권한
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade={CascadeType.ALL})
    @JoinTable(name = "account_roles", joinColumns = {
            @JoinColumn(name = "account_id") },
            inverseJoinColumns = {
            @JoinColumn(name = "role_id") })
    @ToString.Exclude
    private Set<Role> userRoles = new HashSet<>();

    //연관관계의 주인 - cart
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;

    @Builder.Default
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();


    public Account(Long id, String username, String password, int age) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.age = age;
    }

}

