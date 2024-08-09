package com.myboard.toy.security.admin.repository;

import com.myboard.toy.security.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserManagementRepository extends JpaRepository<Account,Long> {
}
