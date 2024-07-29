package com.myboard.toy.application.hello;

import com.myboard.toy.domain.hello.HelloUser;
import com.myboard.toy.infrastructure.hello.HelloUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HelloUserService {

    @Autowired
    private HelloUserRepository repository;

    public HelloUser createUser(HelloUser user) {
        return repository.save(user);
    }

    public HelloUser getUserById(Long id) {
        return repository.findById(id);
    }

    public HelloUser getUserByLoginId(String loginId) {
        return repository.findByLoginId(loginId);
    }

    public HelloUser getUserByEmail(String email) {
        return repository.findByEmail(email);
    }

    public HelloUser updateUser(HelloUser user) {
        return repository.save(user);
    }

    public void deleteUser(Long id) {
        repository.deleteById(id);
    }

    // 비밀번호 재설정
    public void resetPassword(String email, String newPassword) {
        HelloUser user = repository.findByEmail(email);
        if (user != null) {
            user.setPassword(newPassword);
            repository.save(user);  // 업데이트된 비밀번호 저장
        }
    }

}
