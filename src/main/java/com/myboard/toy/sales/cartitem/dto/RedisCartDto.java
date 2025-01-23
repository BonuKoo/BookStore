package com.myboard.toy.sales.cartitem.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisCartDto {

    private Map<String, RedisCartItemDto> items = new HashMap<>(); // 장바구니 아이템 목록
    private int totalPrice; // 총합 금액
    private String status; // 장바구니 상태 (예: "ACTIVE", "INACTIVE")
    private LocalDateTime lastUpdated; // 마지막 업데이트 시간

    public void addItem(RedisCartItemDto item) {
        if (items.containsKey(item.getItemId())) {
            RedisCartItemDto existingItem = items.get(item.getItemId());
            existingItem.addCount(item.getCount());
            updateTotalPrice(item.getItemPrice() * item.getCount());
        } else {
            items.put(item.getItemId(), item);
            updateTotalPrice(item.getItemPrice() * item.getCount());
        }
        updateLastUpdated();
    }

    public void removeItem(String itemId) {
        RedisCartItemDto removedItem = items.remove(itemId);
        if (removedItem != null) {
            updateTotalPrice(-(removedItem.getItemPrice() * removedItem.getCount()));
        }
        updateLastUpdated();
    }

    public void updateTotalPrice(int amount) {
        this.totalPrice += amount;
    }

    public void clearCart() {
        items.clear();
        totalPrice = 0;
        status = "EMPTY";
        updateLastUpdated();
    }

    public void updateLastUpdated() {
        this.lastUpdated = LocalDateTime.now();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return items.isEmpty();
    }


}
