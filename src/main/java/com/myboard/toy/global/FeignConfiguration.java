package com.myboard.toy.global;

import com.myboard.toy.global.error.FeignClientExceptionDecoder;
import feign.FeignException;
import feign.Logger;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration@EnableFeignClients(basePackages = "com.myboard.toy")
@Import(FeignConfiguration.class)
public class FeignConfiguration {

    @Bean
    Logger.Level feignLoggerLevel(){
        return Logger.Level.FULL;
    }

    //TODO
    @Bean
    public ErrorDecoder errorDecoder(){
        return new FeignClientExceptionDecoder();
    }

    public Retryer retryer(){
        return new Retryer.Default(1000,2000,3);
    }

}
