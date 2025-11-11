package com.bookService.core.domain.item.repository;

import com.bookService.core.domain.item.Item;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, String>
        //,ItemRepository4QueryDsl
        {

    Optional<Item> findByIsbn(String isbn);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT i FROM Item i WHERE i.isbn = :isbn")
    Optional<Item> findByIsbnWithOptimisticLock(@Param("isbn") String isbn);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Item i WHERE i.isbn = :isbn")
    Optional<Item> findByIdWithPessimisticLock(@Param("isbn") String isbn);
}
