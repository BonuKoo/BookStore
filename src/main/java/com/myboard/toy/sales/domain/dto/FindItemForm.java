package com.myboard.toy.sales.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindItemForm{
    private String isbn;

    public FindItemForm(String isbn) {
        this.isbn = isbn;
    }
}