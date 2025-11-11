package com.bookService.core.config.batch;

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