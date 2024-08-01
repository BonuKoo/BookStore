package com.myboard.toy.securityproject.security.mapper;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapBasedUrlRoleMapper implements UrlRoleMapper{

    private final LinkedHashMap<String,String> urlRoleMappings = new LinkedHashMap<>();

    @Override
    public Map<String, String> getUrlRoleMappings() {
        urlRoleMappings.put("/", "permitAll");
        //정적자원
        urlRoleMappings.put("/css/**", "permitAll");
        urlRoleMappings.put("/js/**", "permitAll");
        urlRoleMappings.put("/images/**", "permitAll");
        urlRoleMappings.put("/favicon.*", "permitAll");
        urlRoleMappings.put("/*/icon-*", "permitAll");
        //로그인 로직
        urlRoleMappings.put("/signup", "permitAll");
        urlRoleMappings.put("/login", "permitAll");
        urlRoleMappings.put("/logout", "permitAll");
        urlRoleMappings.put("/denied", "authenticated");
        //권한에 따른 접근
        urlRoleMappings.put("/user", "ROLE_USER");
        urlRoleMappings.put("/admin/**", "ROLE_ADMIN");
        urlRoleMappings.put("/manager", "ROLE_MANAGER");
        //DB연동- 표현식
        urlRoleMappings.put("/db", "hasRole('DBA')");

        return urlRoleMappings;
    }
}
