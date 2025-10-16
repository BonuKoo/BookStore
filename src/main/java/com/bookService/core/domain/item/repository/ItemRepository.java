package com.bookService.core.domain.item.repository;

import com.bookService.core.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, String>,ItemRepository4QueryDsl {

    Optional<Item> findByIsbn(String isbn);

}
