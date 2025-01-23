package com.myboard.toy.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisTestRunner implements CommandLineRunner {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void run(String... args) throws Exception {

        // Redis에 데이터 저장
        redisTemplate.opsForValue().set("testKey", "Hello, Redis!");

        // Redis에서 데이터 조회
        String value = redisTemplate.opsForValue().get("testKey");
        System.out.println("Value from Redis: " + value);

    }
}
