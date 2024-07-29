package com.myboard.toy.infrastructure.item;

import com.myboard.toy.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item,Long> {


    public Item findByIsbn(String isbn);

}
