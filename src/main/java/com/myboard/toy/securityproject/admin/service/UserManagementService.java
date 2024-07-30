package com.myboard.toy.securityproject.admin.service;




import com.myboard.toy.securityproject.domain.dto.AccountDto;
import com.myboard.toy.securityproject.domain.entity.Account;

import java.util.List;

public interface UserManagementService {

    void modifyUser(AccountDto accountDto);

    List<Account> getUsers();
    AccountDto getUser(Long id);

    void deleteUser(Long idx);

}