package com.myboard.toy.infrastructure.item.repository;

import com.myboard.toy.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item,Long> {

}
