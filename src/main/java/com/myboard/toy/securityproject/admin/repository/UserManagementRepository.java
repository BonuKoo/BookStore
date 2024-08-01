package com.myboard.toy.securityproject.admin.repository;

import com.myboard.toy.securityproject.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserManagementRepository extends JpaRepository<Account,Long> {
}
