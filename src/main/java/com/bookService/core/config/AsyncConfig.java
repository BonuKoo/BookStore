package com.bookService.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "checkoutTaskExecutor")
    public Executor checkoutTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 핵심 풀 크기 (Core Pool Size): 최소한으로 유지할 스레드 수 (기본 CPU 코어 수 고려)
        executor.setCorePoolSize(120);
        // 최대 풀 크기 (Max Pool Size): 부하가 높을 때 최대로 늘릴 수 있는 스레드 수
        executor.setMaxPoolSize(180);
        // 큐 용량 (Queue Capacity): 스레드가 모두 사용 중일 때 대기열에 쌓을 요청 수
        executor.setQueueCapacity(200);
        // 스레드 이름 접두사 (Thread Name Prefix)
        executor.setThreadNamePrefix("Checkout-Async-");
        // Executor 초기화
        executor.initialize();
        return executor;
    }

}
