package com.bookService.core.domain.todo.controller;

import com.bookService.core.common.dto.ResponseDTO;
import com.bookService.core.domain.todo.dto.TodoDTO;
import com.bookService.core.domain.todo.entity.TodoEntity;
import com.bookService.core.domain.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor    // 생성자 주입을 위한 Lombok 어노테이션
@RestController             // REST 컨트롤러 선언
@RequestMapping("todo")     // "/todo" 경로로 요청 매핑
public class TodoController {
    private final TodoService service;

    //생성
    @PostMapping
    public ResponseEntity<?> createTodo(@AuthenticationPrincipal String userId, @RequestBody TodoDTO dto) {
        try {
            List<TodoDTO> dtos =  service.create(userId,dto);
            // 정상 응답 객체 생성
            ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();
            // 200 OK 응답
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            String error = e.getMessage();             // 예외 메시지 추출
            ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().error(error).build(); // 에러 응답 객체 생성
            return ResponseEntity.badRequest().body(response);  //400 Bad Request 응답
        }
    }

    // List 반환
    @GetMapping
    public ResponseEntity<?> retrieveTodoList(@AuthenticationPrincipal String userId) {
        List<TodoDTO> todoDTOS  = service.retrieve(Long.parseLong(userId));
        ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(todoDTOS).build();
        return ResponseEntity.ok().body(response);
    }

    // 수정
    @PutMapping
    public ResponseEntity<?> updateTodo(@AuthenticationPrincipal String userId, @RequestBody TodoDTO dto) {

        List<TodoDTO> dtos = service.update(userId,dto);

        ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();

        return ResponseEntity.ok().body(response);
    }

    // 삭제
    @DeleteMapping
    public ResponseEntity<?> deleteTodo(@AuthenticationPrincipal String userId, @RequestBody TodoDTO dto) {
        try {

            TodoEntity entity = TodoDTO.toEntity(dto);
            entity.setUserId(Long.parseLong(userId));

            List<TodoDTO> dtos = service.delete(userId,dto);

            ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().data(dtos).build();

            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            String error = e.getMessage();
            ResponseDTO<TodoDTO> response = ResponseDTO.<TodoDTO>builder().error(error).build();
            return ResponseEntity.badRequest().body(response);
        }
    }
}
