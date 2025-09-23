package com.bookService.core.domain.login.controller;

import com.bookService.core.domain.login.dto.AccountDTO;
import com.bookService.core.domain.login.dto.ResponseDTO;
import com.bookService.core.domain.login.entity.AccountEntity;
import com.bookService.core.domain.login.service.AccountService;
import com.bookService.core.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AccountController {

    private final AccountService userService;

    private final TokenProvider tokenProvider; // JWT 토큰 생성 유틸

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody AccountDTO accountDTO) {
        try {
            if (accountDTO == null || accountDTO.getPassword() == null) {
                throw new RuntimeException("Invalid Password value.");
            }

            AccountDTO responseUserDTO = userService.create(accountDTO);

            return ResponseEntity.ok().body(responseUserDTO);
        }
        catch (Exception e) {
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .error(e.getMessage())
                    .build();

            return ResponseEntity.badRequest().body(responseDTO);
        }
    }
    // 로그인 처리 메서드
    @PostMapping("/signin")
    public ResponseEntity<?> authenticate(@RequestBody AccountDTO userDTO) {
        // 입력받은 사용자 정보로 인증 시도
        AccountEntity user = userService.getByCredentials(userDTO.getUsername(), userDTO.getPassword());

        if (user != null) {
            // 인증 성공 시 JWT 토큰 발급
            final String token = tokenProvider.create(user);

            // 응답 객체에 사용자 정보 및 토큰 포함
            final AccountDTO responseUserDTO = AccountDTO.builder()
                    .username(user.getUsername())
                    .id(user.getId())
                    .token(token)
                    .build();
            // 200 OK 응답
            return ResponseEntity.ok().body(responseUserDTO);
        }
        else {
            // 인증 실패 시 에러 응답
            ResponseDTO responseDTO = ResponseDTO.builder()
                    .error("Login failed.")
                    .build();
            // 400 Bad Request
            return ResponseEntity.badRequest().body(responseDTO);
        }
    }
}
