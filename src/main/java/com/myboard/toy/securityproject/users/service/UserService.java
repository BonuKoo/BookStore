package com.myboard.toy.securityproject.users.service;

import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.securityproject.admin.repository.RoleRepository;
import com.myboard.toy.securityproject.domain.entity.Account;
import com.myboard.toy.securityproject.domain.entity.Role;
import com.myboard.toy.securityproject.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Role role = roleRepository.findByRoleName("ROLE_USER");
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        account.setUserRoles(roles);

        Cart cart = Cart.builder()
                .account(account)
                .totPrice(0)
                .build();
        account.setCart(cart);

        userRepository.save(account);

    }

    public Account getAccountByUsername(String username){
        return userRepository.findByUsername(username);
    }

}
