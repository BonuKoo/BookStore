package com.bookService.core.domain.login.entity;

import com.bookService.core.domain.cart.Cart;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = "username") })
public class AccountEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    private String password;

    private String role;

    private String authProvider;

    //연관관계의 주인 - cart
    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;

    public AccountEntity(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public AccountEntity(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
