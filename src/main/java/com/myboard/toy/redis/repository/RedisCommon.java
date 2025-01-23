package com.myboard.toy.redis.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisCommon {

    private final RedisTemplate<String,Object> template;
    private final ObjectMapper objectMapper;

    /**
     * 여러 데이터를 Key-Value 형태로 저장
     */
    public <T> void multiSetData(Map<String, T> datas) {
        Map<String, String> jsonMap = new HashMap<>();

        for (Map.Entry<String, T> entry : datas.entrySet()) {
            try {
                jsonMap.put(entry.getKey(), objectMapper.writeValueAsString(entry.getValue()));
            } catch (Exception e) {
                log.error("Error during serialization", e);
            }
        }

        template.opsForValue().multiSet(jsonMap);
    }

    /**
     * @Param key   Redis
     * @Param field field
     * @Param value 저장할 객체
     * @Param <T>   객체 타입
     * */
    public <T> void putInHash(String key, String field, T value) {
        try {
            //객체를 JSON 문자열로 변환
            String jsonValue = objectMapper.writeValueAsString(value);
            template.opsForHash().put(key, field, jsonValue);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize value for Redis Hash", e);
            throw new RuntimeException("Serialization error", e);
        }
    }

    /**
     * Redis Hash에서 데이터 조회
     *
     * @param key   Redis 키
     * @param field Redis 필드 (Hash의 서브 키)
     * @param clazz 반환할 객체의 클래스 타입
     * @param <T>   반환할 객체의 타입
     * @return Redis에 저장된 객체 또는 null
     */
    public <T> T getFromHash(String key, String field, Class<T> clazz) {
        Object result = template.opsForHash().get(key, field);
        if (result != null) {
            try {
                return objectMapper.readValue(result.toString(), clazz); // JSON 문자열을 객체로 변환
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize value from Redis Hash", e);
                throw new RuntimeException("Deserialization error", e);
            }
        }
        return null;
    }

    /**
     * Hash 자료구조에서 특정 필드 삭제
     */
    public void removeFromHash(String key, String field) {
        template.opsForHash().delete(key, field);
    }

}
