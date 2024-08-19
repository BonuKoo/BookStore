package com.myboard.toy.security.users.controller;

import com.myboard.toy.security.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RestUserDeleteController {

    private final UserService userService;

    @PostMapping("/delete-account")
    public ResponseEntity<Map<String, Object>> deleteAccount(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.deleteAccount(principal); // 실제로 계정을 삭제하는 서비스 로직

            SecurityContextHolder.clearContext();

            response.put("message", "Account deleted successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Failed to delete account.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
