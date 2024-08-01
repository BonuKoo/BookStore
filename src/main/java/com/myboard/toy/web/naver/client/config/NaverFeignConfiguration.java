package com.myboard.toy.web.naver.client.config;

import com.myboard.toy.global.error.FeignClientExceptionDecoder;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NaverFeignConfiguration {

    //Key
    @Value("${naver.key.client_id}")
    private String clientId;
    @Value("${naver.key.client_secret}")
    private String clientSecret;

    @Bean
    Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }

    //TODO
    @Bean
    public ErrorDecoder errorDecoder(){
        return new FeignClientExceptionDecoder();
    }

    //재시도
    public Retryer retryer(){
        return new Retryer.Default(1000,2000,3);
    }


    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("X-Naver-Client-Id", clientId);
            template.header("X-Naver-Client-Secret", clientSecret);
        };
    }

}
