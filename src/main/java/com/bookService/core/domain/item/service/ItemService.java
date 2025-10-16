package com.bookService.core.domain.item.service;

import com.bookService.core.common.exception.ItemNotFoundException;
import com.bookService.core.domain.item.Item;
import com.bookService.core.domain.item.repository.ItemRepository;
import com.bookService.core.infra.naver.dto.NaverBookDetailViewResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(NaverBookDetailViewResponseDto dto){

        Optional<Item> findIsbn = itemRepository.findByIsbn(dto.getChannel().getItems().get(0).getIsbn());

        if (findIsbn.isEmpty()){
            int discount = Integer.parseInt(dto.getChannel().getItems().get(0).getDiscount());
            int defau_Quantity = 100;

            Item item = new Item(dto.getChannel().getItems().get(0).getIsbn(),
                    dto.getChannel().getItems().get(0).getTitle(),
                    discount,
                    defau_Quantity
            );

            itemRepository.save(item);
        }
    }

    public Item findByIsbn(String isbn) {
        return itemRepository.findByIsbn(isbn)
                .orElseThrow(()->new ItemNotFoundException("해당하는 상품은 존재하지 않습니다."));
    }

    @Transactional
    public void decrease(String isbn, int quantity){

        Item item = itemRepository.findByIsbn(isbn)
                .orElseThrow();
        item.removeStock(quantity);
        itemRepository.saveAndFlush(item);
    };


}
