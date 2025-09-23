package com.bookService.core.config;

import com.bookService.core.domain.cart.Cart;
import com.bookService.core.domain.cart.repository.CartRepository;
import com.bookService.core.domain.cartitem.CartItem;
import com.bookService.core.domain.item.Item;
import com.bookService.core.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Random;

//@Configuration
@RequiredArgsConstructor
public class CartItemBatchConfig {

    /*
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;

    private static final int START_CART_ID = 2; // Cart PK 기준
    private static final int END_CART_ID = 10002;
    private static final int CHUNK_SIZE = 100;
    private static final int CART_ITEM_COUNT = 5; // CartItem 개수 고정
    private static final int MAX_ISBN = 9_999_999;

    @Bean
    public Job cartItemInsertJob(Step cartItemInsertStep) {
        return new JobBuilder("cartItemInsertJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(cartItemInsertStep)
                .build();
    }

    @Bean
    public Step cartItemInsertStep() {
        return new StepBuilder("cartItemInsertStep", jobRepository)
                .<Cart, Cart>chunk(CHUNK_SIZE, transactionManager)
                .reader(cartItemReader())
                .processor(cartItemProcessor())
                .writer(cartItemWriter())
                .build();
    }

    // Reader: Cart 범위 조회
    @Bean
    public ItemReader<Cart> cartItemReader() {
        List<Cart> carts = cartRepository.findAllWithItemsByIdBetween((long) START_CART_ID, (long) END_CART_ID);
        return new ListItemReader<>(carts);
    }

    // Processor: CartItem 5개 생성 후 Cart에 추가
    @Bean
    public ItemProcessor<Cart, Cart> cartItemProcessor() {
        return cart -> {
            Random random = new Random();

            for (int i = 0; i < CART_ITEM_COUNT; i++) {
                int isbnNum = 1 + random.nextInt(7);
                String isbn = String.format("ISBN%07d", isbnNum);

                // 중복 Item 방지
                boolean alreadyExists = cart.getCartItems().stream()
                        .anyMatch(ci -> ci.getItem().getIsbn().equals(isbn));
                if (alreadyExists) continue;

                Item item = itemRepository.findById(isbn).orElse(null);
                if (item == null) continue; // 존재하지 않는 Item 스킵

                CartItem cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setItem(item);
                cartItem.setAmount(5);

                cart.addCartItem(cartItem); // totPrice 계산 포함
            }

            return cart;
        };
    }

    // Writer: Cart 저장 (CartItem cascade)
    @Bean
    public ItemWriter<Cart> cartItemWriter() {
        return carts -> cartRepository.saveAll(carts);
    }
    */
}
