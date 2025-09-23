package com.bookService.core.domain.todo.repository;

import com.bookService.core.domain.login.entity.AccountEntity;
import com.bookService.core.domain.todo.entity.TodoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoJpaRepository extends JpaRepository<TodoEntity, Long> {

    List<TodoEntity> findByUserId(Long userId);
    
    //userId로 단일 TodoEntity를 조회
    @Query("SELECT t FROM TodoEntity t WHERE t.userId = ?1")
    TodoEntity findByUserIdQuery(Long userId);

}
