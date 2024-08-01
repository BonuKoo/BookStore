package com.myboard.toy.infrastructure.hello;

import com.myboard.toy.domain.hello.HelloUser;
import com.myboard.toy.domain.hello.dto.HelloUserDto;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class HelloUserRepository {

    private final Map<Long, HelloUser> storeById = new HashMap<>();
    private final Map<String, HelloUser> storeByLoginId = new HashMap<>();
    private final Map<String, HelloUser> storeByEmail = new HashMap<>();
    private long sequence = 0L;

    public HelloUser save(HelloUser user){
        if (user.getId() == null) {
            user.setId(++sequence);
        }
        storeById.put(user.getId(), user);
        storeByLoginId.put(user.getLoginId(), user);
        storeByEmail.put(user.getEmail(), user);
        return user;
    }

    public HelloUser findById(Long id){
        return storeById.get(id);
    }

    public HelloUser findByLoginId(String loginId){
        return storeByLoginId.get(loginId);
    }

    public HelloUser findByEmail(String email){
        return storeByEmail.get(email);
    }

    public void deleteById(Long id) {
        HelloUser user = storeById.remove(id);
        if (user != null) {
            storeByLoginId.remove(user.getLoginId());
            storeByEmail.remove(user.getEmail());
        }
    }
}

