package com.bookService.core.domain.cartitem;

import com.bookService.core.domain.cart.Cart;
import com.bookService.core.domain.item.Item;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "isbn")
    private Item item;

    private int amount; //상품 개수

    public CartItem(Cart cart, Item item, int amount) {
        this.cart = cart;
        this.item = item;
        this.amount = amount;
    }

    public void addCount(int count){
        this.amount += count;
    }

    public void updateCount(int count) {
        this.amount = count;
    }

    //== private setter ==//
    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public void setItem(Item item) {
        this.item = item;
    }

}
