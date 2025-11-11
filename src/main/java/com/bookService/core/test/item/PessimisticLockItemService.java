package com.bookService.core.test.item;

import com.bookService.core.domain.item.Item;
import com.bookService.core.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PessimisticLockItemService {

    private final ItemRepository itemRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void decrease(String id, int quantity) {

        // findByIdWithPessimisticLock 쿼리를 통해 해당 Item 로우에 PESSIMISTIC_WRITE 락을 건다.
        // 다른 트랜잭션은 락이 해제될 때까지 이 로우에 대한 쓰기 접근이 차단된다.
        Item item = itemRepository.findByIdWithPessimisticLock(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id));

        // 재고 감소 비즈니스 로직 실행
        item.decrease(quantity);
        itemRepository.saveAndFlush(item);
    }

    /** Decrease 보상용 Roll-Back increase 메서드 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increase_1(String id, int quantity){
        Item item = itemRepository.findByIdWithPessimisticLock(id)
                .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id));
        item.increaseStock(quantity);
        itemRepository.saveAndFlush(item);
    }
}
