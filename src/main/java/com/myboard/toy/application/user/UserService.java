package com.myboard.toy.application.user;

import com.myboard.toy.domain.user.User;
import com.myboard.toy.infrastructure.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void join(User user){
        userRepository.save(user);
    }

}

