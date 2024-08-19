package com.myboard.toy.sales.item.repository;

import com.myboard.toy.sales.domain.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item,String> {

    //SetUpData 저장을 위함
    Optional<Item> findByIsbn(String isbn);

}

