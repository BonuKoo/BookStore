package com.myboard.toy.security.security.service;


import com.myboard.toy.security.security.mapper.UrlRoleMapper;

import java.util.Map;

public class DynamicAuthorizationService {

    private final UrlRoleMapper delegate;

    public DynamicAuthorizationService(UrlRoleMapper delegate) {
        this.delegate = delegate;
    }

    public Map<String ,String> getUrlRoleMappings(){
        return delegate.getUrlRoleMappings();
    }
}
