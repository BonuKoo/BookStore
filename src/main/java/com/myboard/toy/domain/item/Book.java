package com.myboard.toy.domain.item;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;


//@DiscriminatorValue("B")
//@Entity
public class Book
        //extends Item
{

    private String isbn;

    //private String author;


    /*
    *   실험용 setter
    * */
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

}

/*
    이 프로젝트에선 Book만 다룸
    -> item을 일단 Public Class로 바꾸고
    엔티티 자체로 다뤄버리자.
 */
