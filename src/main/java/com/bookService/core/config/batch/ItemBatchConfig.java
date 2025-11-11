package com.bookService.core.config.batch;

/*
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j*/
public class ItemBatchConfig {
/*
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ItemRepository itemRepository;

    private static final int TOTAL_ITEMS = 60_000;
    private static final int CHUNK_SIZE = 100;

    // Job 정의
    @Bean
    public Job itemInsertJob(Step itemInsertStep) {
        return new JobBuilder("itemInsertJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(itemInsertStep)
                .build();
    }

    // Step 정의
    @Bean
    public Step itemInsertStep() {
        return new StepBuilder("itemInsertStep", jobRepository)
                .<Item, Item>chunk(CHUNK_SIZE, transactionManager)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    // Reader: Item 생성
    @Bean
    @StepScope
    public ItemReader<Item> itemReader() {
        return new ItemReader<>() {
            private int index = 2000;

            @Override
            public Item read() {
                if (index > TOTAL_ITEMS) return null;

                String isbn = buildIsbn(index);
                String title = "Book Title " + index;
                int price = 1000 + (index % 1000); // 가격 랜덤화
                int stock = 10 + (index % 50);     // 재고 랜덤화

                Item item = new Item(isbn, title, price, stock);
                index++;
                return item;
            }
        };
    }

    // Processor: 단순 통과
    @Bean
    @StepScope
    public ItemProcessor<Item, Item> itemProcessor() {
        return item -> item;
    }

    // Writer: JpaRepository saveAll
    @Bean
    @StepScope
    public ItemWriter<Item> itemWriter() {
        return items -> itemRepository.saveAll(items);
    }

    // ISBN 생성 (중복 방지)
    private String buildIsbn(int index) {
        return String.format("ISBN%07d", index);
    }*/

}
