package com.myboard.toy.domain.naver.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class NaverBookDetailViewResponseDto {

    @JacksonXmlProperty(localName = "title")
    private String title;

    @JacksonXmlProperty(localName = "link")
    private String link;

    @JacksonXmlProperty(localName = "image")
    private String image;

    @JacksonXmlProperty(localName = "author")
    private String author;

    @JacksonXmlProperty(localName = "discount")
    private String discount;

    @JacksonXmlProperty(localName = "publisher")
    private String publisher;

    @JacksonXmlProperty(localName = "pubdate")
    private String pubdate;

    @JacksonXmlProperty(localName = "isbn")
    private String isbn;

    @JacksonXmlProperty(localName = "description")
    private String description;
}