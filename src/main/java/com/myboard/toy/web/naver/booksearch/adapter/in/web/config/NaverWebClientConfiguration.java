package com.myboard.toy.web.naver.booksearch.adapter.in.web.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NaverWebClientConfiguration {

    //Key
    @Value("${naver.api.client_id}")
    private String clientId;
    @Value("${naver.api.client_secret}")
    private String clientSecret;

    @Bean("naverRequestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("X-Naver-Client-Id", clientId);
            template.header("X-Naver-Client-Secret", clientSecret);
        };
    }
}

