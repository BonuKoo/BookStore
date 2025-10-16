package com.bookService.core.domain.item.repository.impl;


import com.bookService.core.domain.item.Item;
import com.bookService.core.domain.item.QItem;
import com.bookService.core.domain.item.repository.ItemRepository4QueryDsl;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public class ItemRepository4QueryDslImpl implements ItemRepository4QueryDsl {

    private final JPAQueryFactory queryFactory;

    QItem item = QItem.item;

    public ItemRepository4QueryDslImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Optional<Item> findByIdWithPessimisticLock(String isbn) {
        return Optional.ofNullable(queryFactory
                .selectFrom(item)
                .where(item.isbn.eq(isbn))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE) // 낙관적 락 설정
                .fetchOne());
    }

    @Override
    public Optional<Item> findByIdWithOptimisticLoc(String isbn) {
        return Optional.ofNullable(queryFactory
                .selectFrom(item)
                .where(item.isbn.eq(isbn))
                .setLockMode(LockModeType.OPTIMISTIC) // 낙관적 락 설정
                .fetchOne());    }
}
