package com.bookService.core.infra.naver.dto;

import lombok.Getter;

@Getter
public class NaverBookDetailRequestDto {

    private String isbn;

    public NaverBookDetailRequestDto(String isbn) {
        this.isbn = isbn;
    }

}
