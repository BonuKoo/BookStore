package com.myboard.toy.sales.cartitem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisCartItemDto {


    private String itemId; // Item의 ID (예: ISBN)
    private String itemName; // 상품 이름
    private int count; // 상품 개수
    private int itemPrice; // 상품 가격

    public void addCount(int count) {
        this.count += count;
    }

    public void updateCount(int count) {
        this.count = count;
    }
}
