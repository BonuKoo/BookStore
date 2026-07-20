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

    // 판매자가 지정되지 않은 상품(네이버 검색 결과 등)의 기본 판매자 — "미지정/플랫폼" 취급.
    // 결제 확정 이벤트가 이 값을 그대로 들고 워커까지 가므로, sellerId를 null로 남기면
    // ledger-worker/settlement-worker가 언박싱 시 NPE를 낸다. 항상 값이 있도록 보장한다.
    public static final Long UNASSIGNED_SELLER_ID = 0L;

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
        this.sellerId = UNASSIGNED_SELLER_ID;
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
