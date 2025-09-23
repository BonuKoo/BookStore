package com.bookService.core.domain.login.service;

import com.bookService.core.domain.login.dto.AccountDTO;
import com.bookService.core.domain.login.entity.AccountEntity;
import com.bookService.core.domain.login.repository.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

 @Slf4j
@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountJpaRepository userRepository;

    // 비밀번호 암호화를 위한 인코더
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // (회원가입)
    public AccountDTO create(AccountDTO accountDTO) {
        if (accountDTO == null || accountDTO.getUsername() == null) {
            throw new RuntimeException("Invalid arguments");
        }

        //Account 객체 생성
        AccountEntity accountEntity = new AccountEntity(accountDTO.getUsername(),
                passwordEncoder.encode(accountDTO.getPassword()) );

        final String username = accountEntity.getUsername();

        if (userRepository.existsByUsername(username)) {
            log.warn("Username already exists {}", username);
            throw new RuntimeException("Username already exists");
        }

        AccountEntity savedAccountEntity = userRepository.save(accountEntity);

        AccountDTO responseUserDTO = AccountDTO.builder()
                .id(savedAccountEntity.getId())
                .username(savedAccountEntity.getUsername())
                .build();

        return responseUserDTO;
    }

    // (로그인) 사용자 인증 메서드 : username과 password를 비교하여 사용자 반환
    public AccountEntity getByCredentials(final String username, final String password) {
        final AccountEntity originalUser = userRepository.findByUsername(username);
        // 사용자 존재 및 비밀번호 일치 여부 확인
        if (originalUser != null && passwordEncoder.matches(password, originalUser.getPassword())) {
            return originalUser;    // 인증 성공 시 사용자 객체 반환
        }

        return null;    //인증 실패 시 null 반환
    }

    // Account 반환
    public AccountDTO getAccountById(Long userId){
        Optional<AccountEntity> accountOpt = userRepository.findById(userId);
        AccountEntity accountEntity = accountOpt.orElseThrow();

        AccountDTO accountDTO = AccountDTO.builder()
                .id(accountEntity.getId())
                .username(accountEntity.getUsername())
                .build();

        return accountDTO;
    }
}