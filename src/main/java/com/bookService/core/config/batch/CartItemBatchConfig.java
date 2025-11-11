package com.bookService.core.config.batch;

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
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//@Configuration
@RequiredArgsConstructor
public class CartItemBatchConfig {


    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;

    private static final int START_CART_ID = 3; // Cart PK 기준
    private static final int END_CART_ID = 9002;
    private static final int CHUNK_SIZE = 10;
    private static final int CART_ITEM_COUNT = 6; // CartItem 개수 고정
//    private static final int MAX_ISBN = 9_999_999;

    // 장바구니에 넣을 ISBN 목록 515 ~ 520, 521, 560,
    private static final List<String> TARGET_ISBNs = IntStream.range(521, 560)
            .mapToObj(i -> String.format("ISBN%07d", i))
            .collect(Collectors.toList());

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

    // Processor: 대상 ISBN 6개를 Cart에 추가 (랜덤 대신 고정된 목록 사용)
    @Bean
    public ItemProcessor<Cart, Cart> cartItemProcessor() {
        // 미리 Item 엔티티를 모두 로드하여 캐시합니다. (N+1 방지)
        List<Item> items = itemRepository.findAllById(TARGET_ISBNs);

        return cart -> {
            // CartItem이 이미 존재하는지 확인 (불필요한 중복 삽입 방지)
            if (cart.getCartItems().size() >= CART_ITEM_COUNT) {
                return cart; // 이미 충분한 아이템이 있다면 스킵
            }

            int newlyAddedCount = 0;

            for (String isbn : TARGET_ISBNs) {
                // Item 캐시에서 해당 Item을 찾습니다.
                Item item = items.stream()
                        .filter(i -> i.getIsbn().equals(isbn))
                        .findFirst()
                        .orElse(null);

                if (item == null) continue;

                // 이미 장바구니에 해당 ISBN이 있는지 확인
                boolean alreadyExists = cart.getCartItems().stream()
                        .anyMatch(ci -> ci.getItem().getIsbn().equals(isbn));
                if (alreadyExists) continue;

                // 1. CartItem 생성 및 추가
                CartItem cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setItem(item);
                cartItem.setAmount(5);

                cart.addCartItem(cartItem);

                // 2. 카운트 증가
                newlyAddedCount++;

                //  핵심 수정: 6개를 채웠다면 루프를 즉시 중단합니다.
                if (newlyAddedCount >= CART_ITEM_COUNT) {
                    break;
                }
            }

            return cart;
        };
    }

    // Writer: Cart 저장 (CartItem cascade)
    @Bean
    public ItemWriter<Cart> cartItemWriter() {
        return carts -> cartRepository.saveAll(carts);
    }

}
