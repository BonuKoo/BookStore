package com.bookService.core.domain.cart;

import com.bookService.core.domain.cartitem.CartItem;
import com.bookService.core.domain.login.entity.AccountEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @OneToOne(cascade = CascadeType.ALL,orphanRemoval = true)
    @OneToOne
    @JoinColumn(name = "account_id")
    private AccountEntity account;

    @OneToMany(mappedBy = "cart",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    private int totPrice; //카트에 담긴 상품의 가격 총합

    public Cart(AccountEntity account, List<CartItem> cartItems, int totPrice) {
        this.account = account;
        this.cartItems = cartItems;
        this.totPrice = totPrice;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    // == TotPrice == //
    public void setTotPrice(int totPrice) {
        this.totPrice = totPrice;
    }

    public void updateTotPrice(int amount){
        //this.totPrice += amount;
    }

    public void recalcTotPrice() {
        this.totPrice = cartItems.stream()
                .mapToInt(ci -> ci.getAmount() * ci.getItem().getPrice())
                .sum();
    }

    public void addCartItem(CartItem cartItem){
        this.cartItems.add(cartItem);
        cartItem.setCart(this);
        recalcTotPrice();
    }

    public void removeCartItem(CartItem cartItem) {
        this.cartItems.remove(cartItem);
        cartItem.setCart(null);
        recalcTotPrice();  // 삭제 시에도 재계산
    }

}
