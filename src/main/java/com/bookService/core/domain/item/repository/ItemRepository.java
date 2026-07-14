package com.bookService.core.domain.item.repository;

import com.bookService.core.domain.item.Item;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
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

    /**
     * 조건부 원자 차감. 읽고-계산하고-쓰는 방식(removeStock)과 달리 UPDATE 한 문장으로
     * lost update와 음수 재고를 동시에 차단한다. 재고가 부족하면 0행이 갱신되므로
     * 반환값으로 성공(1)/재고부족(0)을 구분한다.
     * (JPQL 벌크 연산은 @Version을 건드리지 않는다 — 이 메서드는 락 기반 메서드들과
     *  섞어 쓰지 않고 재고 차감 컨슈머 전용으로 사용한다)
     */
    @Modifying
    @Query("UPDATE Item i SET i.stockQuantity = i.stockQuantity - :quantity " +
           "WHERE i.isbn = :isbn AND i.stockQuantity >= :quantity")
    int deductStock(@Param("isbn") String isbn, @Param("quantity") int quantity);
}
