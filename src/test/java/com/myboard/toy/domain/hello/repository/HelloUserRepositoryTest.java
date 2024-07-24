package com.myboard.toy.domain.hello.repository;

import com.myboard.toy.domain.hello.HelloUser;
import com.myboard.toy.infrastructure.hello.repository.HelloUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HelloUserRepositoryTest {

    @Autowired
    HelloUserRepository repository;

    @BeforeEach
    public void setUp(){
        //데이터 초기화
        repository = new HelloUserRepository();
    }

    @Test
    void save() {

        // given
        HelloUser user = new HelloUser(null,
                "user1",
                "password1",
                LocalDate.of(1990, 1, 1),
                "zxcqew32@naver.com",
                "123-456-7890");
        // when
        HelloUser savedUser = repository.save(user);

        // then
        HelloUser foundById = repository.findById(savedUser.getId());
        HelloUser foundByLoginId = repository.findByLoginId("user1");
        HelloUser foundByEmail = repository.findByEmail("zxcqew32@naver.com");

        assertEquals(savedUser, foundById);
        assertEquals(savedUser, foundByLoginId);
//        assertEquals(savedUser, foundByEmail);
    }

    @Test
    public void saveDuplicateUserTest() {
        // given
        HelloUser user1 = new HelloUser(null, "user2", "password2", LocalDate.of(1991, 2, 2), "user2@example.com", "234-567-8901");
        HelloUser user2 = new HelloUser(null, "user2", "password3", LocalDate.of(1992, 3, 3), "user2@example.com", "345-678-9012");

        // when
        HelloUser savedUser1 = repository.save(user1);
        HelloUser savedUser2 = repository.save(user2);

        // then
        HelloUser foundByLoginId = repository.findByLoginId("user2");
        HelloUser foundByEmail = repository.findByEmail("user2@example.com");

        assertEquals(savedUser2, foundByLoginId);
        assertEquals(savedUser2, foundByEmail);
        assertNull(repository.findById(savedUser1.getId())); // user1 should be overwritten
    }

}