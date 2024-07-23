package com.myboard.toy.domain.item;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;


@DiscriminatorValue("B")
@Entity
public class Book extends Item{

    private String author;
    private String isbn;

}

/*
    이 프로젝트에선 Book만 다룸
    - 네이버 Book API 다뤄야 하기 때문
 */
