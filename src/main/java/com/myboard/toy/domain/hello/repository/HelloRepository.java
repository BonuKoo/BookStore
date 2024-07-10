package com.myboard.toy.domain.hello.repository;

import com.myboard.toy.domain.hello.Hello;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class HelloRepository {
    private final Map<Long, Hello> store = new HashMap<>();
    private long sequence = 0L;

    public Hello save(Hello hello){
        hello.setId(++sequence);
        store.put(hello.getId(), hello);
        return hello;
    }

    public Hello findById(Long id){
        return store.get(id);
    }
}
