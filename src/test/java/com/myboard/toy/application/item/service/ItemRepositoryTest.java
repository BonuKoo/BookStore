package com.myboard.toy.application.item.service;

import com.myboard.toy.domain.item.Item;
import com.myboard.toy.infrastructure.item.ItemRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
class ItemRepositoryTest {

    //@Autowired
    private ItemRepository itemRepository;

    //@Test
    public void 책_등록_시험_Success(){

        //Given
        /*
            재고 등록
            AWS 시스템 개발 스킬업
            Isbn과 price
            재고

         */
        Item item = new Item();
        item.setIsbn("9791193926246");
        item.setPrice(17820);
        item.setStockQuantity(99);

        //저장
        itemRepository.save(item);

        //When
        Item book1Test = itemRepository.findByIsbn("9791193926246");

        //Then

        Assertions.assertThat(book1Test.getIsbn()).isEqualTo(item.getIsbn());
        Assertions.assertThat(book1Test.getPrice()).isEqualTo(item.getPrice());

    }



}