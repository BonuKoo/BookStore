package com.myboard.toy.security.users.controller;

import com.myboard.toy.security.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RestUserExistController {

        private final UserService userService;


    @PostMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        boolean exists = userService.checkExistByUsername(username);

        // 응답 메시지와 결과를 담을 Map 생성
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);

        // exists 값에 따라 다른 메시지를 설정
        if (exists==true) {
            response.put("message", "이미 사용중인 아이디입니다.");
        } else {
            response.put("message", "사용 가능한 아이디입니다.");
        }

        return ResponseEntity.ok(response);
    }
}
