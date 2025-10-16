package com.bookService.core.config;

import com.bookService.core.domain.login.entity.AccountEntity;
import com.bookService.core.domain.login.repository.AccountJpaRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

//@Configuration
//@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j

public class AccountBatchConfig {

    /*
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AccountJpaRepository accountJpaRepository;
    private final PasswordEncoder passwordEncoder;
*/
    /*
    private static final int TOTAL_ACCOUNTS = 110_000;
    private static final int CHUNK_SIZE = 1000;


    // Job 정의
    @Bean
    public Job accountInsertJob(Step accountInsertStep) {
        return new JobBuilder("accountInsertJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(accountInsertStep)
                .build();
    }

    // Step 정의 (Bean으로 주입)

    @Bean
    public Step accountInsertStep() throws Exception {
        return new StepBuilder("accountInsertStep", jobRepository)
                .<AccountEntity, AccountEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(accountReader())
                .processor(accountProcessor())
                .writer(accountWriter())
                .build();
    }

    // Reader: JpaPagingItemReader (StepScope 필수)
    @Bean
    public ItemReader<AccountEntity> accountReader() {
        return new ItemReader<>() {
            private int index = 40005;
            @Override
            public AccountEntity read() {
                if (index > TOTAL_ACCOUNTS) return null;
                AccountEntity account = new AccountEntity();
                account.setUsername(buildUsername(index));
                account.setPassword(passwordEncoder.encode("password"));
                account.setRole("USER");
                index++;
                return account;
            }
        };
    }



    // Processor: DTO -> Entity + 패스워드 암호화
    @Bean
    @StepScope
    public ItemProcessor<AccountEntity, AccountEntity> accountProcessor() {
        return entity -> {
            entity.setPassword(passwordEncoder.encode("password"));
            entity.setRole("USER");
            return entity;
        };
    }

    // Writer: JpaRepository saveAll
    @Bean
    @StepScope
    public ItemWriter<AccountEntity> accountWriter() {
        return items -> accountJpaRepository.saveAll(items);
    }

    // 계정 이름 생성 유틸
    private String buildUsername(int index) {
        int base = (index - 1) % 10_000 + 1;
        int suffixIndex = (index - 1) / 10_000;
        String suffix = "";
        if (suffixIndex > 0) {
            char ch = (char) ('a' + (suffixIndex - 1));
            suffix = String.valueOf(ch);
        }
        return "testUser" + base + suffix;
    }*/
}
