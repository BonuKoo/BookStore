package com.bookService.core.config.batch;

/*
import com.bookService.core.domain.cart.dto.CartDocument;
import com.bookService.core.domain.cartitem.dto.CartItemDocument;
*/
import lombok.RequiredArgsConstructor;
//import org.springframework.data.mongodb.core.MongoTemplate;


//@Configuration
@RequiredArgsConstructor
public class BatchAccountMongoCartConfig {

    /*
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AccountJpaRepository accountJpaRepository;
    private final MongoTemplate mongoTemplate;

    private static final int CHUNK_SIZE = 100;
*/
    /*
    @Bean
    public Job accountToMongoCartJob(Step accountToCartStep) {
        return new JobBuilder("accountToMongoCartJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(accountToCartStep)
                .build();
    }
     */
    /*
    @Bean
    public Step accountToCartStep() {
        return new StepBuilder("accountToMongoCartStep", jobRepository)
                .<AccountEntity, CartDocument>chunk(CHUNK_SIZE, transactionManager)
                .reader(accountRandomReader())
                .processor(accountToCartProcessor())
                .writer(cartMongoWriter())
                .build();
    }
*/
    /*
    // Reader: 랜덤하게 1000명 추출
    @Bean
    public ItemReader<AccountEntity> accountRandomReader() {
        return new IteratorItemReader<>(accountJpaRepository.findRandomAccounts(1000));
    }
*/
    // Processor: AccountEntity → CartDocument 변환
    /*
    @Bean
    public ItemProcessor<AccountEntity, CartDocument> accountToCartProcessor() {
        return account -> {
            CartDocument cart = new CartDocument();
            cart.setUserId(account.getUsername());
            cart.setItems(List.of(
                    new CartItemDocument("ISBN0000001", 5) // 초기값
            ));
            return cart;
        };
    }*/
    /*
    // Writer: MongoDB에 저장
    @Bean
    public ItemWriter<CartDocument> cartMongoWriter() {
        return items -> mongoTemplate.insertAll(items.getItems());
    }*/
}
