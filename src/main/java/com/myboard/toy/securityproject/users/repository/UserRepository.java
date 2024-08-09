package com.myboard.toy.securityproject.users.repository;

import com.myboard.toy.securityproject.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Account,Long> {

    Account findByUsername(String username);
    Account findByUsernameIgnoreCase(String username);

}
