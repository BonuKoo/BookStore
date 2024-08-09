package com.myboard.toy.web.naver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NaverBookListRequestDto {
    private String query;
    private String display;
    private String start;
    private String sort;

}
