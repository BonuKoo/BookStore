package com.myboard.toy.infrastructure.item;

import com.myboard.toy.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item,String> {

    //SetUpData 저장을 위함
    Optional<Item> findByIsbn(String isbn);

}
