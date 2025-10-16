package com.bookService.core.config;

import com.bookService.core.domain.cart.Cart;
import com.bookService.core.domain.cart.repository.CartRepository;
import com.bookService.core.domain.login.entity.AccountEntity;
import com.bookService.core.domain.login.repository.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
/*
@Configuration
@RequiredArgsConstructor
@EnableBatchProcessing*/
public class CartBatchConfig {
/*
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AccountJpaRepository accountJpaRepository;
    private final CartRepository cartRepository;

    private static final int START_ACCOUNT_ID = 5;
    private static final int END_ACCOUNT_ID = 10005;
    private static final int CHUNK_SIZE = 100;

    // Job 정의

    @Bean
    public Job cartInsertJob(Step cartInsertStep) {
        return new JobBuilder("cartInsertJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(cartInsertStep)
                .build();
    }

    // Step 정의
    @Bean
    public Step cartInsertStep() {
        return new StepBuilder("cartInsertStep", jobRepository)
                .<AccountEntity, Cart>chunk(CHUNK_SIZE, transactionManager)
                .reader(cartAccountReader())
                .processor(cartProcessor())
                .writer(cartWriter())
                .build();
    }

    // Reader: Account 조회
    @Bean
    public ItemReader<AccountEntity> cartAccountReader() {
        List<AccountEntity> accounts = accountJpaRepository.findAllByIdBetween(
                (long) START_ACCOUNT_ID, (long) END_ACCOUNT_ID
        );
        return new IteratorItemReader<>(accounts);
    }

    // Processor: Cart 생성
    @Bean
    public ItemProcessor<AccountEntity, Cart> cartProcessor() {
        return account -> {
            Cart cart = new Cart();
            cart.setAccount(account);
            cart.setTotPrice(0);
            return cart;
        };
    }

    // Writer: Cart 저장
    @Bean
    public ItemWriter<Cart> cartWriter() {
        return carts -> cartRepository.saveAll(carts);
    }*/
}