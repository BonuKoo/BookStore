package com.myboard.toy.listener;

import com.myboard.toy.domain.item.Book;
import com.myboard.toy.domain.item.Item;
import com.myboard.toy.infrastructure.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SetupBookDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetupTwo = false;

    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetupTwo){
            return;
        }
    }


    public void createBookIfNotFound(String title,String isbn,int price,
                                     int stockQuantity){
        Item item = itemRepository.findByIsbn(isbn);

        if (item == null){
            item = Item.builder()
                    .title(title)
                    .isbn(isbn)
                    .price(price)
                    .stockQuantity(stockQuantity)
                    .build();
        }
        itemRepository.save(item);
    }
}
