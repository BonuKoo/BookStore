package com.bookService.core.test.item;

import com.bookService.core.domain.item.Item;
import com.bookService.core.domain.item.repository.ItemRepository;
import com.bookService.core.test.item.exception.StockUnderflowException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OptimisticLockItemService {

    private final ItemRepository itemRepository;

    /** 재고 감소 decrease 메서드 */
    @Transactional(propagation = Propagation.REQUIRED)
    public void decrease1(String id , int quantity){
        Item item = itemRepository.findByIsbnWithOptimisticLock(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id));

        // 2. 재고 감소 비즈니스 로직
        if (item.getStockQuantity() < quantity) {
            throw new StockUnderflowException("재고가 부족합니다.");
        }
        item.decrease(quantity);

        itemRepository.saveAndFlush(item);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decrease2(String id , int quantity){
        Item item = itemRepository.findByIsbnWithOptimisticLock(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id));

        // 2. 재고 감소 비즈니스 로직
        if (item.getStockQuantity() < quantity) {
            throw new StockUnderflowException("재고가 부족합니다.");
        }
        item.decrease(quantity);

        itemRepository.saveAndFlush(item);
    }


    /** Decrease 보상용 Roll-Back increase 메서드 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increase_1(String id, int quantity){
        Item item = itemRepository.findByIsbnWithOptimisticLock(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id));
        item.increaseStock(quantity);
        itemRepository.saveAndFlush(item);
    }

    /** Decrease 보상용 Roll-Back increase 메서드 */
    @Transactional(propagation = Propagation.REQUIRES_NEW) // ✅ 독립적인 트랜잭션으로 커밋 보장
    public void increaseAsync(String id, int quantity){
        Item item = itemRepository.findByIsbnWithOptimisticLock(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id));
        item.increaseStock(quantity);
        itemRepository.saveAndFlush(item);
    }
}
