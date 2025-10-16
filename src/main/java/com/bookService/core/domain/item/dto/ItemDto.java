package com.bookService.core.domain.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private String isbn;
    private String title;
    private int price;
    private Long sellerId;

}
