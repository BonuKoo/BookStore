package com.myboard.toy.application.order;

import com.myboard.toy.common.exception.ItemNotFoundException;
import com.myboard.toy.common.exception.UserNotFoundException;
import com.myboard.toy.domain.address.Address;
import com.myboard.toy.domain.delivery.Delivery;
import com.myboard.toy.domain.delivery.DeliveryStatus;
import com.myboard.toy.domain.item.Item;
import com.myboard.toy.domain.order.Order;
import com.myboard.toy.domain.orderitem.OrderItem;
import com.myboard.toy.infrastructure.item.ItemRepository;
import com.myboard.toy.infrastructure.order.OrderRepository;
import com.myboard.toy.securityproject.domain.entity.Account;
import com.myboard.toy.securityproject.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void orderV2_shouldCreateOrderSuccessfully() {
        // Given
        Long accountId = 1L;
        String isbn = "1234567890";
        int count = 2;

        Account account = new Account();
        account.setId(accountId);
        account.setAddress(new Address("City", "123", "12345"));

        Item item = new Item();
        item.setIsbn(isbn);
        item.setPrice(100);
        item.setStockQuantity(50);

        Delivery delivery = new Delivery(account.getAddress(), DeliveryStatus.READY);

        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        Order order = Order.createOrder(account, delivery, orderItem);

        order.setId(1L);

        // When
        when(userRepository.findById(accountId)).thenReturn(java.util.Optional.of(account));
        when(itemRepository.findByIsbn(isbn)).thenReturn(item);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Test
        Long orderId = orderService.orderV2(accountId, isbn, count);

        // Then
        assertEquals(order.getId(), orderId);

        verify(userRepository, times(1)).findById(accountId);
        verify(itemRepository, times(1)).findByIsbn(isbn);
        verify(orderRepository, times(1)).save(any(Order.class));
    }




    @Test
    void orderV2_shouldThrowUserNotFoundException() {
        // Given
        Long accountId = 1L;
        String isbn = "1234567890";
        int count = 2;

        // When
        when(userRepository.findById(accountId)).thenReturn(java.util.Optional.empty());

        // Then
        assertThrows(UserNotFoundException.class, () -> {
            orderService.orderV2(accountId, isbn, count);
        });

        verify(userRepository, times(1)).findById(accountId);
        verify(itemRepository, times(0)).findByIsbn(isbn);
        verify(orderRepository, times(0)).save(any(Order.class));
    }

    @Test
    void orderV2_shouldThrowItemNotFoundException() {
        // Given
        Long accountId = 1L;
        String isbn = "1234567890";
        int count = 2;

        Account account = new Account();
        account.setId(accountId);

        // When
        when(userRepository.findById(accountId)).thenReturn(java.util.Optional.of(account));
        when(itemRepository.findByIsbn(isbn)).thenReturn(null);

        // Then
        assertThrows(ItemNotFoundException.class, () -> {
            orderService.orderV2(accountId, isbn, count);
        });

        verify(userRepository, times(1)).findById(accountId);
        verify(itemRepository, times(1)).findByIsbn(isbn);
        verify(orderRepository, times(0)).save(any(Order.class));
    }
}
