package com.myboard.toy.application.item.service;

import com.myboard.toy.common.exception.ItemNotFoundException;
import com.myboard.toy.domain.item.Item;
import com.myboard.toy.domain.naver.dto.NaverBookDetailViewResponseDto;
import com.myboard.toy.infrastructure.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;


    //Save를 저장, Optional로 isbn 해당하는 값이 있는 지 없는 지

    @Transactional
    public void saveItem(NaverBookDetailViewResponseDto dto){

        Optional<Item> findIsbn = itemRepository.findByIsbn(dto.getChannel().getItems().get(0).getIsbn());

        if (findIsbn.isEmpty()){
            int discount = Integer.parseInt(dto.getChannel().getItems().get(0).getDiscount());
            int defau_Quantity = 9999;

            Item item = Item.builder()
                    .isbn(dto.getChannel().getItems().get(0).getIsbn())
                    .title(dto.getChannel().getItems().get(0).getTitle())
                    .price(discount)
                    .stockQuantity(defau_Quantity)
                    .build();
            itemRepository.save(item);
        }

    }

    public List<Item> findItems(){

        return itemRepository.findAll();

    }


    public Item findByIsbn(String isbn) {
        return itemRepository.findById(isbn)
                .orElseThrow(()->new ItemNotFoundException("해당하는 상품은 존재하지 않습니다."));
    }

}
