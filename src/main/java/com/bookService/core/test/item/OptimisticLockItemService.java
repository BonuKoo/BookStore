package com.bookService.core.test.item;

import com.bookService.core.domain.item.Item;
import com.bookService.core.domain.item.repository.ItemRepository;
import com.bookService.core.domain.item.service.ItemService;
import com.bookService.core.test.item.exception.StockUnderflowException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OptimisticLockItemService {

    private final ItemRepository itemRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decrease(String id , int quantity){
        Item item = itemRepository.findByIdWithOptimisticLoc(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id));

        // 2. 재고 감소 비즈니스 로직
        if (item.getStockQuantity() < quantity) {
            throw new StockUnderflowException("재고가 부족합니다.");
        }
        item.decrease(quantity);

        itemRepository.save(item);
    }

}
