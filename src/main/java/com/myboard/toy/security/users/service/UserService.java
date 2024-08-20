package com.myboard.toy.security.users.service;

import com.myboard.toy.common.exception.UserNotFoundException;
import com.myboard.toy.order.domain.Address;
import com.myboard.toy.sales.domain.entity.Cart;
import com.myboard.toy.security.admin.repository.RoleRepository;
import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.domain.entity.Role;
import com.myboard.toy.security.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

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

    //String -> 조회는 username이 아니라 id로..
    public Account getAccountById(Long id){
        Account account = userRepository.findById(id).orElseThrow();
        return account;
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

    @Transactional
    public void updateAccount(AccountDto dto,Principal principal){

        Account account = getAccountByPrincipal(principal);

        //비밀번호 암호화 처리
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()){
            String encodedPassword = passwordEncoder.encode(dto.getPassword());
            dto.setPassword(encodedPassword);
        }

        // Address 필드가 null인지 확인하고 초기화
        if(account.getAddress()==null){
            account.setAddress(new Address());
        }

        account.update(dto);

        userRepository.save(account);
    }

    public AccountDto getProfile(Principal principal){

        Account account = getAccountByPrincipal(principal);
        if (account.getAddress()==null){
            account.setAddress(new Address());
        }
        return AccountDto.builder()
                .username(account.getUsername())
                .nickname(account.getNickname())
                .age(account.getAge())
                .postcode(account.getAddress().getPostcode())
                .roadAddress(account.getAddress().getRoadAddress())
                .jibunAddress(account.getAddress().getJibunAddress())
                .detailAddress(account.getAddress().getDetailAddress())
                .extraAddress(account.getAddress().getExtraAddress())
                .build();
    }

    public String getAddress(AccountDto dto){
        Long id = dto.getId();
        Optional<Account> accountOpt = userRepository.findById(id);

        if (accountOpt.isPresent()){

            Account account = accountOpt.get();
            Address address = account.getAddress();

            //StringBuilder를 사용해서 주소 문자열 생성
            StringBuilder addressBuilder = new StringBuilder();

            if (address.getPostcode() != null) {
                addressBuilder.append(address.getPostcode()).append(" ");
            }
            if (address.getRoadAddress() != null) {
                addressBuilder.append(address.getRoadAddress()).append(" ");
            }
            if (address.getJibunAddress() != null) {
                addressBuilder.append(address.getJibunAddress()).append(" ");
            }
            if (address.getDetailAddress() != null) {
                addressBuilder.append(address.getDetailAddress()).append(" ");
            }
            if (address.getExtraAddress() != null) {
                addressBuilder.append(address.getExtraAddress());
            }

            //String으로 반환
            return addressBuilder.toString().trim();
        } else {
            throw new RuntimeException("Account not found with id: " + id);
        }

    }

    @Transactional
    public void deleteAccount(Principal principal){
        AccountDto accountDto = getAccountDtoFromPrincipal(principal);
        Long id = accountDto.getId();
        userRepository.deleteById(id);
    }

    public AccountDto getAccountIdByPrincipal(Principal principal){
        Account account = getAccountByPrincipal(principal);
        return AccountDto.builder()
                .id(account.getId())
                .build();
    }
    // Principal을 통해 AccountDto를 얻고, pricinpal을 단순 반환
    public AccountDto getUserDetailsByPrincipal(Principal principal) {
        return getAccountDtoFromPrincipal(principal);
    }

    // Principal을 통해 Account을 얻는다.
    private Account getAccountByPrincipal(Principal principal) {
        AccountDto accountDto = getAccountDtoFromPrincipal(principal);
        Long id = accountDto.getId();
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
    }

    // Principal을 통해 AccountDto를 얻는 공통 메서드
    private AccountDto getAccountDtoFromPrincipal(Principal principal) {
        UsernamePasswordAuthenticationToken authenticationToken = (UsernamePasswordAuthenticationToken) principal;
        return (AccountDto) authenticationToken.getPrincipal();
    }

}
