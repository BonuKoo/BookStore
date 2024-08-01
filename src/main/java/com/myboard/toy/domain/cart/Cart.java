package com.myboard.toy.domain.cart;

import com.myboard.toy.domain.cartitem.CartItem;
import com.myboard.toy.securityproject.domain.entity.Account;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //어차피 1:1이니까
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    @Builder.Default
    @OneToMany(mappedBy = "cart")
    private List<CartItem> cartItems = new ArrayList<>();

    //날짜도 포함하자 나중에
    private int count; //카트에 담긴 상품 개수

    private void setAccount(Account account) {
        this.account = account;
    }

    private void setCount(int count) {
        this.count = count;
    }

    public static Cart createCart(Account account){
        Cart cart = new Cart();
        cart.setCount(0);
        cart.setAccount(account);
        return cart;
    }

}
