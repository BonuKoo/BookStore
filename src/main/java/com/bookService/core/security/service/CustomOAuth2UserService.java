package com.bookService.core.security.service;

import com.bookService.core.domain.login.entity.AccountEntity;
import com.bookService.core.domain.login.repository.AccountJpaRepository;
import com.bookService.core.security.dto.OAuthAttributes;
import com.bookService.core.security.vo.CustomUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private AccountJpaRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("loadUser");

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        log.info("loadUser registrationId = " + registrationId);
        log.info("loadUser userNameAttributeName = " + userNameAttributeName);

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        String nameAttributeKey = attributes.getNameAttributeKey();
        String name = attributes.getName();
        String email = attributes.getEmail();
        String picture = attributes.getPicture();
        String id = attributes.getId();
        String socialType = "";

        if("naver".equals(registrationId)) {
            socialType = "naver";
        }
        else if("kakao".equals(registrationId)) {
            socialType = "kakao";
        }
        else if("github".equals(registrationId)) {
            socialType = "github";

            if(email == null) {
                log.info("loadUser userRequest.getAccessToken().getTokenValue() = " + userRequest.getAccessToken().getTokenValue());

                email = getEmailFromGitHub(userRequest.getAccessToken().getTokenValue());

                log.info("loadUser GitHub email = " + email);
            }
        }
        else {
            socialType = "google";
        }

        log.info("loadUser nameAttributeKey = " + nameAttributeKey);
        log.info("loadUser id = " + id);
        log.info("loadUser socialType = " + socialType);
        log.info("loadUser name = " + name);
        log.info("loadUser email = " + email);
        log.info("loadUser picture = " + picture);

        log.info("loadUser attributes = " + attributes);

        if(name == null) name = "";
        if(email == null) email = "";

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        authorities.add(authority);

        String username = email;
        String authProvider = socialType;
        AccountEntity userEntity = null;

        if(!userRepository.existsByUsername(username)) {

            userEntity = new AccountEntity(username,authProvider);

            userEntity = userRepository.save(userEntity);

        } else {
            userEntity = userRepository.findByUsername(username);
        }

        log.info("Successfully pulled user info username {} authProvider {}", username, authProvider);

        return new CustomUser(userEntity.getId(), email, name, authorities, attributes);
    }

    private String getEmailFromGitHub(String accessToken) {
        String url = "https://api.github.com/user/emails";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

        List<Map<String, Object>> emails = response.getBody();

        if (emails != null) {
            for (Map<String, Object> emailData : emails) {
                if ((Boolean) emailData.get("primary")) {
                    return (String) emailData.get("email");
                }
            }
        }

        return null;
    }

}
