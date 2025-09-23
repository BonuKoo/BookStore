package com.bookService.core.infra.toss.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
@Slf4j
public class TossWebClientConfiguration {

    @Value("${PSP.toss.url}")
    private String baseUrl;

    @Value("${PSP.toss.secretKey}")
    private String secretKey;

    @PostConstruct
    public void init() {
//        System.out.println("✅ Toss URL = " + baseUrl);
//        System.out.println("✅ Toss Secret Key = " + secretKey);
    }

    @Bean
    public String authorizationHeader() {
        String encodedSecretKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
        return "Basic " + encodedSecretKey;
    }
}
