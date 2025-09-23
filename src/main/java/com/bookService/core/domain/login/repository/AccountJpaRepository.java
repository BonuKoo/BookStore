package com.bookService.core.domain.login.repository;

import com.bookService.core.domain.login.entity.AccountEntity;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {

    AccountEntity findByUsername(String username);

    Boolean existsByUsername(String username);

    AccountEntity findByUsernameAndPassword(String username, String password);

    Optional<AccountEntity> findTopByUsernameStartingWithOrderByUsernameDesc(String prefix);

    @Query(value = "SELECT * FROM account_entity ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<AccountEntity> findRandomAccounts(@Param("limit") int limit);

    List<AccountEntity> findAllByIdBetween(Long start, Long end);

}
