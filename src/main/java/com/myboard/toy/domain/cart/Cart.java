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

    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @Builder.Default
    @OneToMany(mappedBy = "cart",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    private int totPrice; //카트에 담긴 상품의 가격 총합

    private void setAccount(Account account) {
        this.account = account;
    }


    public Cart createCart(Account account){
        Cart cart = new Cart();
        cart.setTotPrice(0);
        cart.setAccount(account);
        return cart;
    }

    // == TotPrice == //
    private void setTotPrice(int totPrice) {
        this.totPrice = totPrice;
    }

    public void updateTotPrice(int amount){
        this.totPrice += amount;
    }

    public void addCartItem(CartItem cartItem){
        this.cartItems.add(cartItem);
        updateTotPrice(cartItem.getCount() * cartItem.getItem().getPrice());
    }

}
