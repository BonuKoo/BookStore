package com.myboard.toy.domain.bucket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemToBucketDTO {

    private String title;
    private String isbn;
    private int discount;

}
