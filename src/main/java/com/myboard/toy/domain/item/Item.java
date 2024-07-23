package com.myboard.toy.domain.item;

import com.myboard.toy.common.exception.NotEnoughStockException;
import com.myboard.toy.domain.category.Category;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name ="dtype")
@Entity
public abstract class Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    private String name;        //제품명
    private int price;          //가격
    private int stockQuantity;  //재고

    //TODO : Category 나중에 다 대 다 중간 테이블로 풀어야 함
    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

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
}
