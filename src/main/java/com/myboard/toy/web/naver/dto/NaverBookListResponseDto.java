package com.myboard.toy.web.naver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class NaverBookListResponseDto {

    @JsonProperty("lastBuildDate")
    private String lastBuildDate;

    @JsonProperty("total")
    private int total;

    @JsonProperty("start")
    private int start;

    @JsonProperty("display")
    private int display;

    @JsonProperty("items")
    private List<NaverBookDetailResponseDto> items;

    @Data
    public static class NaverBookDetailResponseDto {

        @JsonProperty("title")
        private String title;

        @JsonProperty("link")
        private String link;

        @JsonProperty("image")
        private String image;

        @JsonProperty("author")
        private String author;

        @JsonProperty("discount")
        private int discount;

        @JsonProperty("publisher")
        private String publisher;

        @JsonProperty("pubdate")
        private String pubdate;

        @JsonProperty("isbn")
        private String isbn;

        @JsonProperty("description")
        private String description;

        private String discountFormatted;

    }
}
