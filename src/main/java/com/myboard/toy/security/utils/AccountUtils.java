package com.myboard.toy.security.utils;

import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountUtils {

    private final UserService userService;

    public Account getAccountByPrincipal(UsernamePasswordAuthenticationToken principal) {

        UsernamePasswordAuthenticationToken authenticationToken = principal;
        AccountDto accountDto = (AccountDto) authenticationToken.getPrincipal();
        String username = accountDto.getUsername();

        // username을 토대로 account 값을 db에서 조회한다.
        return userService.getAccountByUsername(username);
    }

    /*
    TODO
    public Long getAccountIdByPrincipal(Principal principal){
        String name = principal.getName();

    }
    */

}
