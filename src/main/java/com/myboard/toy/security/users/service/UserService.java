package com.myboard.toy.security.users.service;

import com.myboard.toy.sales.domain.Cart;
import com.myboard.toy.security.admin.repository.RoleRepository;
import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.domain.entity.Role;
import com.myboard.toy.security.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public void createUser(Account account){

        //회원가입 -> 일반 유저
        Role role = roleRepository.findByRoleName("ROLE_USER");
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        account.setUserRoles(roles);

        Account savedAccount = userRepository.save(account);

        Cart cart = Cart.builder()
                .account(savedAccount)
                .totPrice(0)
                .build();
        account.setCart(cart);

        userRepository.save(savedAccount);

    }

    public Account getAccountByUsername(String username){
        return userRepository.findByUsername(username);
    }


    public AccountDto getAccountIdByPrincipal(Principal principal){
        String username = principal.getName();
        Long accountId = userRepository.findAccountIdByUsername(username);
        return AccountDto.builder()
                .id(accountId)
                .build();
    }


    public boolean checkExistByUsername(String username) {

        Account account = userRepository.findByUsernameIgnoreCase(username);
        if (account != null){
            log.info("이미 사용중인 아이디입니다.");
            return true;
        }else {
            log.info("사용가능한 아이디입니다.");
            return false;
        }
    }

    public Account getAccountByPrincipal(Principal principal) {
        return userRepository.findByUsername(principal.getName());

    }

}
