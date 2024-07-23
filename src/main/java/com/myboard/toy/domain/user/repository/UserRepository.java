package com.myboard.toy.domain.user.repository;

import com.myboard.toy.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

}
