package com.myboard.toy.security.admin.repository;

import com.myboard.toy.security.domain.entity.Role;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class RoleRepositoryTest {


    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findByRoleName() {
    }

    @Test
    void findAllRolesWithoutExpression() {

        List<Role> allRoles = roleRepository.findAll();

        for (Role role : allRoles) {
            log.info("role: {}", role);
        }

    }

}
