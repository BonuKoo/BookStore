package com.myboard.toy.domain.user.service;

import com.myboard.toy.domain.user.User;
import com.myboard.toy.domain.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    /* 회원 가입*/
    public Long join(User user){

        userRepository.save(user);
        return user.getId();
    }

    private void validateDuplicateUser(User user){
        List<User> findUsers =
                userRepository.findByName(user.getName());
        if (!findUsers.isEmpty()) {

            throw new IllegalStateException("이미 존재하는 회원");
        }
    }
}
