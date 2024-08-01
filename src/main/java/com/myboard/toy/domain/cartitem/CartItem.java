package com.myboard.toy.domain.cartitem;

import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.domain.item.Item;
import jakarta.persistence.*;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class CartItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    private int count; //상품 개수

    public static CartItem createCartItem(Cart cart, Item item, int amount){
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setItem(item);
        cartItem.setCount(amount);
        return cartItem;
    }

    public void addCount(int count){
        this.count += count;
    }

    //== private setter ==//
    private void setCart(Cart cart) {
        this.cart = cart;
    }

    private void setItem(Item item) {
        this.item = item;
    }

    private void setCount(int count) {
        this.count = count;
    }
}
