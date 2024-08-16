package com.myboard.toy.web.naver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
public class NaverBookListRequestDto {
    private String query;
    private String display;
    private String start;
    private String sort;

    @Builder
    public NaverBookListRequestDto(String query, String display, String start, String sort) {
        this.query = query;
        this.display = display;
        this.start = start;
        this.sort = sort;
    }
}
