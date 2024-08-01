package com.myboard.toy.securityproject.admin.service;


import com.myboard.toy.securityproject.domain.entity.Resources;

import java.util.List;

public interface ResourcesService {
    Resources getResources(long id);
    List<Resources> getResources();

    void createResources(Resources Resources);

    void deleteResources(long id);
}