package com.myboard.toy.securityproject.domain.entity;

import com.myboard.toy.domain.address.Address;
import com.myboard.toy.domain.board.Board;
import com.myboard.toy.domain.bucket.Bucket;
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
    @GeneratedValue
    private Long id;

    @Column
    private String username;

    @Column
    private int age;

    @Column
    private String password;

    @Builder.Default
    @OneToMany(mappedBy = "account",cascade = CascadeType.ALL,orphanRemoval = true)
    List<Board> boards = new ArrayList<>();


    @Builder.Default
    @OneToMany(mappedBy = "account",cascade = CascadeType.ALL,orphanRemoval = true)
    List<Reply> replys = new ArrayList<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade={CascadeType.MERGE})
    @JoinTable(name = "account_roles", joinColumns = {
            @JoinColumn(name = "account_id") },
            inverseJoinColumns = {
            @JoinColumn(name = "role_id") })
    @ToString.Exclude
    private Set<Role> userRoles = new HashSet<>();

    @Embedded
    private Address address;

    @Builder.Default
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Bucket bucket;


    public Account(Long id, String username, String password, int age) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.age = age;
    }

    public void setBucket(Bucket bucket) {
        this.bucket = bucket;
        bucket.setAccount(this);
    }

    // 장바구니 초기화 메서드
    public void initializeBucket() {
        if (this.bucket != null) {
            this.bucket.getItems().clear();
        } else {
            this.bucket = new Bucket();
            this.bucket.setAccount(this);
        }
    }
}