package com.myboard.toy.securityproject.admin.repository;


import com.myboard.toy.securityproject.domain.entity.RoleHierarchy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleHierarchyRepository extends JpaRepository<RoleHierarchy, Long> {
}