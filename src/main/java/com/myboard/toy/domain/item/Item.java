package com.myboard.toy.domain.item;

import com.myboard.toy.common.exception.NotEnoughStockException;
import com.myboard.toy.domain.category.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
//@DiscriminatorColumn(name ="dtype")
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @Column(name = "isbn",unique = true)
    private String isbn;        //ISBN 번호
    private String title;

    private int price;          //가격
    private int stockQuantity;  //재고

    /*
    //TODO : Category 나중에 다 대 다 중간 테이블로 풀어야 함
    @Builder.Default
    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();
    */

    //재고 추가
    public void addStock(int quantity){
        this.stockQuantity += quantity;
    }

    //재고 줄어듬
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;

        if (restStock < 0) {
            throw new NotEnoughStockException();
        }
        this.stockQuantity = restStock;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

}
