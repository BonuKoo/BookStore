package com.bookService.core.domain.todo.service;

import com.bookService.core.domain.todo.dto.TodoDTO;
import com.bookService.core.domain.todo.entity.TodoEntity;
import com.bookService.core.domain.todo.repository.TodoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class TodoService {

    private final TodoJpaRepository repository;

    public List<TodoDTO> create(String userId,TodoDTO todoDTO) {

        TodoEntity entity = TodoDTO.toEntity(todoDTO);  //dto -> Entity 변환
        entity.setId(null);
        entity.setUserId(Long.parseLong(userId));

        validate(entity);

        repository.save(entity);

        log.info("Entity Id : {} is saved.", entity.getId());
        // 엔티티 리스트 -> DTO 리스트로 변환
        List<TodoEntity> todoEntities = repository.findByUserId(entity.getUserId());
        List<TodoDTO> dtos = todoEntities.stream().map(TodoDTO::new).collect(Collectors.toList());
        return dtos;
    }
    // 데이터 저장 전 유효성 검사
    private void validate(final TodoEntity entity) {
        if (entity == null) {
            log.warn("Entity cannot be null.");
            throw new RuntimeException("Entity cannot be null.");
        }

        if (entity.getUserId() == null) {
            log.warn("Unknown user.");
            throw new RuntimeException("Unknown user.");
        }
    }
    // 특정 사용자의 모든 TodoEntity 항목 조회
    public List<TodoDTO> retrieve(final Long userId) {
        List<TodoEntity> todoEntities = repository.findByUserId(userId);
        List<TodoDTO> dtos = todoEntities.stream().map(TodoDTO::new).collect(Collectors.toList());
        return dtos;
    }

    public List<TodoDTO> update(String userId, TodoDTO dto) {

        TodoEntity entity = TodoDTO.toEntity(dto);
        entity.setUserId(Long.parseLong(userId));
        validate(entity);

        final Optional<TodoEntity> original = repository.findById(entity.getId());

        original.ifPresent(todo -> {
            todo.setTitle(entity.getTitle());
            todo.setDone(entity.isDone());

            repository.save(todo);
        });

        return retrieve(entity.getUserId());
    }

    public List<TodoDTO> delete(String userId,TodoDTO dto) {
        TodoEntity entity = TodoDTO.toEntity(dto);
        entity.setUserId(Long.parseLong(userId));
        validate(entity);

        try {
            repository.delete(entity);
        } catch (Exception e) {
            log.error("error deleting entity ", entity.getId(), e);

            throw new RuntimeException("error deleting entity " + entity.getId());
        }

        return retrieve(entity.getUserId());
    }

}
