package com.bookService.core.domain.item;

import com.bookService.core.domain.cartitem.CartItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @Column(name = "isbn",unique = true)
    private String isbn;        //ISBN 번호
    private String title;
    private int price;          //가격
    private int stockQuantity;  //재고

    private Long sellerId; // 판매자 Id

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();


    public Item(String isbn, String title, int price, int stockQuantity) {
        this.isbn = isbn;
        this.title = title;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    //재고 추가
    public void addStock(int quantity){
        this.stockQuantity += quantity;
    }

    //재고 줄어듬
    public void removeStock(int quantity) {

        if (this.stockQuantity - quantity < 0) {
            throw new RuntimeException("재고는 0개 미만이 될 수 없습니다.");
        }

        this.stockQuantity -= quantity;

    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

}
