package com.myboard.toy.domain.naver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class NaverBookListRequestDto {
    private String query;
    private String display;
    private String start;
    private String sort;

}
