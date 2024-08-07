package com.myboard.toy.securityproject.security.listener;


import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.infrastructure.cart.CartRepository;
import com.myboard.toy.securityproject.admin.repository.ResourcesRepository;
import com.myboard.toy.securityproject.admin.repository.RoleHierarchyRepository;
import com.myboard.toy.securityproject.admin.repository.RoleRepository;
import com.myboard.toy.securityproject.domain.entity.Account;
import com.myboard.toy.securityproject.domain.entity.Resources;
import com.myboard.toy.securityproject.domain.entity.Role;
import com.myboard.toy.securityproject.domain.entity.RoleHierarchy;
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
public class SetupDataLoader
        implements ApplicationListener<ContextRefreshedEvent>
{

    private boolean alreadySetup = false;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleHierarchyRepository roleHierarchyRepository;
    private final ResourcesRepository resourcesRepository;
    private final CartRepository cartRepository;

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

        // Role 데이터 삽입
        HashSet<Role> roles = new HashSet<>();

        Role admin = createRoleIfNotFound("ROLE_ADMIN", "관리자");
        Role manager = createRoleIfNotFound("ROLE_MANAGER","매니저");
        Role user = createRoleIfNotFound("ROLE_USER","유저");

        // Resources 데이터 삽입
        Resources resource1 = createResourceIfNotFound(1L, "/admin/**", "GET", 1, "URL");
        Resources resource2 = createResourceIfNotFound(2L, "/manager/**", "GET", 2, "URL");
        Resources resource3 = createResourceIfNotFound(3L, "/user/**", "GET", 3, "URL");

        // RoleHierarchy 데이터 삽입
        RoleHierarchy adminRole = createRoleHierarchyIfNotFound(1L, "ROLE_ADMIN", null);
        RoleHierarchy managerRole = createRoleHierarchyIfNotFound(2L, "ROLE_MANAGER", adminRole.getId());
        createRoleHierarchyIfNotFound(3L, "ROLE_DBA", adminRole.getId());
        createRoleHierarchyIfNotFound(4L, "ROLE_USER", managerRole.getId());
        createRoleHierarchyIfNotFound(5L, "ROLE_USER", adminRole.getId());

        // User Dummy 데이터 삽입
        createUserIfNotFound("admin", "admin@admin.com", "pass", Set.of(admin));

        createUserIfNotFound("simseoyeon","simseoyeon@google.com","1234", Set.of(manager));

        createUserIfNotFound("zzonduk","zzonduk@naver.com","omulomul", Set.of(user));


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

    public void createUserIfNotFound(final String userName,final String email,final String password,final Set<Role> roleSet) {


        Account account = userRepository.findByUsername(userName);

        if (account == null) {
            account = Account.builder()
                    .username(userName)
                    .password(passwordEncoder.encode(password))
                    .userRoles(roleSet)
                    .build();

            //계정이 존재하지 않는 경우, 우선 cart를 만들고 account를 저장.
            Cart cart = Cart.builder()
                    .account(account)
                    .totPrice(0)
                    .build();
            cartRepository.save(cart);
            //cart 저장 -> cart가 연관관계 주인이니까 account도 저장됨

        }
    }

    private Resources createResourceIfNotFound(Long id, String resourceName, String httpMethod, int orderNum, String resourceType) {
        Resources resource = resourcesRepository.findById(id).orElse(null);
        if (resource == null) {
            resource = Resources.builder()
                    .id(id)
                    .resourceName(resourceName)
                    .httpMethod(httpMethod)
                    .orderNum(orderNum)
                    .resourceType(resourceType)
                    .build();
            resourcesRepository.save(resource);
        }
        return resource;
    }

    private RoleHierarchy createRoleHierarchyIfNotFound(Long id, String roleName, Long parentId) {
        RoleHierarchy roleHierarchy = roleHierarchyRepository.findById(id).orElse(null);
        if (roleHierarchy == null) {
            RoleHierarchy parent = parentId == null ? null : roleHierarchyRepository.findById(parentId).orElse(null);
            roleHierarchy = RoleHierarchy.builder()
                    .id(id)
                    .roleName(roleName)
                    .parent(parent)
                    .build();
            roleHierarchyRepository.save(roleHierarchy);
        }
        return roleHierarchy;
    }

}

