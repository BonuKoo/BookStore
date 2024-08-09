package com.myboard.toy.web.naver.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

@Data
public class NaverBookDetailViewResponseDto {

    @JacksonXmlProperty(localName = "channel")
    private Channel channel;

    @Data
    public static class Channel{

        @JacksonXmlProperty(localName = "title")
        private String title;

        @JacksonXmlProperty(localName = "link")
        private String link;

        @JacksonXmlProperty(localName = "description")
        private String description;

        @JacksonXmlProperty(localName = "lastBuildDate")
        private String lastBuildDate;

        @JacksonXmlProperty(localName = "total")
        private Integer total;

        @JacksonXmlProperty(localName = "start")
        private Integer start;

        @JacksonXmlProperty(localName = "display")
        private Integer display;

        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "item")
        private List<Item> items;

    @Data
    public static class Item{

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
    }
}