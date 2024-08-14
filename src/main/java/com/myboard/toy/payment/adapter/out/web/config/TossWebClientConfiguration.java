package com.myboard.toy.payment.adapter.out.web.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class TossWebClientConfiguration {

    @Value("${toss.api.url}")
    private String baseUrl;

    @Value("${toss.api.secretKey}")
    private String secretKey;

    @Bean("tossRequestInterceptor")
    public RequestInterceptor requestInterceptor() {
        String encodedSecretKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return template -> {
            template.header("Authorization", "Basic " + encodedSecretKey);
            template.header("Content-Type", "application/json");
        };
    }
}
