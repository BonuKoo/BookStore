package com.myboard.toy.web.naver.dto;

import lombok.Getter;


@Getter
public class NaverBookDetailRequestDto {
    private String isbn;

    public NaverBookDetailRequestDto(String isbn) {
        this.isbn = isbn;
    }
}