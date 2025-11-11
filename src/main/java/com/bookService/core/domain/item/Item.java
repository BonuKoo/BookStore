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

    @Id @Column(name = "isbn",unique = true)
    private String isbn;        //ISBN 번호
    private String title;
    private int price;          //가격
    private int stockQuantity;  //재고
    private Long sellerId; // 판매자 Id

    // Lock을 위한 버전 필드
    @Version private Long version;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    public Item(String isbn, String title, int price, int stockQuantity) {
        this.isbn = isbn;
        this.title = title;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    //재고 추가
    public void increaseStock(int quantity){
        this.stockQuantity += quantity;
    }

    //재고 감소
    public void removeStock(int quantity) {
        if (this.stockQuantity - quantity < 0) {
            // 재고 부족에 대한 명확한 예외를 사용하는 것이 좋습니다.
            throw new RuntimeException("재고가 부족합니다. (요청 수량: " + quantity + ", 현재 재고: " + this.stockQuantity + ")");
        }
        this.stockQuantity -= quantity;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void decrease(int quantity){
        removeStock(quantity);
    }

}
