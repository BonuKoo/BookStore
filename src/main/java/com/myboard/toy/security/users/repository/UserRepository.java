package com.myboard.toy.security.users.repository;

import com.myboard.toy.security.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<Account,Long> {

    Account findByUsername(String username);
    Account findByUsernameIgnoreCase(String username);

    @Query("SELECT a.id FROM Account a WHERE a.username = :username")
    Long findAccountIdByUsername(String username);

}
