package com.myboard.toy.application.item.service;

import com.myboard.toy.common.exception.ItemNotFoundException;
import com.myboard.toy.domain.item.Item;
import com.myboard.toy.infrastructure.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public void saveItem(Item item){
        itemRepository.save(item);
    }

    public List<Item> findItems(){

        return itemRepository.findAll();

    }

    public Item findOne(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(()->new ItemNotFoundException("해당하는 상품은 존재하지 않습니다."+itemId));
    }

}
