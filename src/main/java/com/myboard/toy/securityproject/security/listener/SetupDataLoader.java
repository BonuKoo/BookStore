package com.myboard.toy.securityproject.security.listener;


import com.myboard.toy.securityproject.admin.repository.RoleRepository;
import com.myboard.toy.securityproject.domain.entity.Account;
import com.myboard.toy.securityproject.domain.entity.Role;
import com.myboard.toy.securityproject.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {
    private boolean alreadySetup = false;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    @Transactional
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }
        setupData();
        alreadySetup = true;
    }

    private void setupData() {
        HashSet<Role> roles = new HashSet<>();
        Role adminRole = createRoleIfNotFound("ROLE_ADMIN", "관리자");
        Role managerRole = createRoleIfNotFound("ROLE_MANAGER","매니저");
        Role userRole = createRoleIfNotFound("ROLE_USER","유저");


        createUserIfNotFound("admin", "admin@admin.com", "pass", roles);
        roles.add(adminRole);
        createUserIfNotFound("manager","manager@google.com","pass",roles);
        createUserIfNotFound("simseoyeon","simseoyeon@google.com","1234",roles);
        roles.add(managerRole);
        createUserIfNotFound("zzonduk","zzonduk@naver.com","omulomul",roles);
        roles.add(userRole);
        createUserIfNotFound("chocobar","choco@google.com","choco",roles);
        createUserIfNotFound("daldidan","daldidan@kakao.com","bamyangang",roles);
        createUserIfNotFound("hand","hand@daum.com","cream",roles);
        createUserIfNotFound("head","hand@google.com","set",roles);
        createUserIfNotFound("kaka","kakaotalk@naver.com","otalk",roles);

    }

    public Role createRoleIfNotFound(String roleName, String roleDesc) {
        Role role = roleRepository.findByRoleName(roleName);

        if (role == null) {
            role = Role.builder()
                    .roleName(roleName)
                    .roleDesc(roleDesc)
                    .build();
        }
        return roleRepository.save(role);
    }
    //원래 final인데 한번 그냥 변수로 바꿔보자.
    public void createUserIfNotFound( String userName, String email, String password, Set<Role> roleSet) {
        Account account = userRepository.findByUsername(userName);

        if (account == null) {
            account = Account.builder()
                    .username(userName)
                    .password(passwordEncoder.encode(password))
                    .userRoles(roleSet)
                    .build();
        }
        userRepository.save(account);
    }
}