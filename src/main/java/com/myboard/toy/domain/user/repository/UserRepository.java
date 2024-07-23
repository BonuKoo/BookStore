package com.myboard.toy.domain.user.repository;

import com.myboard.toy.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Long> {


    List<User> findByUser(String name);

    List<User> findByName(String name);

}
