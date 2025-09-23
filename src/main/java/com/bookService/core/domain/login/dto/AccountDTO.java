package com.bookService.core.domain.login.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private Long id;
    private String token;
    private String username;
    private String password;

    public AccountDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
