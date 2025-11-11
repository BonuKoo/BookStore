package com.bookService.core.test.checkout.service;

import com.bookService.core.common.exception.checkout.BusinessLogicException;
import com.bookService.core.common.exception.checkout.CheckoutErrorCode;
import com.bookService.core.common.exception.checkout.DataConflictWithDataException;
import com.bookService.core.common.util.IdempotencyCreator;
import com.bookService.core.domain.cartitem.dto.CheckoutItemForQueryProjection2;
import com.bookService.core.domain.checkout.dto.CheckoutCommandForDev;
import com.bookService.core.domain.checkout.dto.CheckoutRequest;
import com.bookService.core.domain.checkout.dto.CheckoutResult;
import com.bookService.core.domain.login.entity.AccountEntity;
import com.bookService.core.domain.payment.dto.PaymentCheckoutOptDtoForQueryProjection;
import com.bookService.core.domain.payment.entity.PaymentEvent;
import com.bookService.core.domain.payment.entity.PaymentOrder;
import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentEventRepository;
import com.bookService.core.domain.payment.usecase.CheckoutFindExistingOrderUseCase;
import com.bookService.core.facade.OptimisticLockStockFacade;
import com.bookService.core.test.item.OptimisticLockItemService;
import com.bookService.core.test.item.PessimisticLockItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutServiceForDev2 {

    private final SpringDataJpaPaymentEventRepository paymentEventRepository;
    private final CheckoutFindExistingOrderUseCase checkoutFindExistingOrderService;
    private final CheckoutReadService checkoutReadService;
    private final OptimisticLockStockFacade optimisticLockStockFacade;
    private final OptimisticLockItemService optimisticLockItemService;
    private final PessimisticLockItemService pessimisticLockStockFacade;
    private final PessimisticLockItemService pessimisticLockItemService;

    // OptimisticLockVer
    @Transactional
    public CheckoutResult checkoutOptimisticLock_1(String userId, CheckoutRequest request) {
        // 1. Checkout 객체 생성
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);
        // 2. CartItem 조회 -> CartItem의 연관관계를 이용해서 Item Data를 가져온다.
        List<CheckoutItemForQueryProjection2> cartItems =
                checkoutReadService.getCartItemsForEventCreation(checkoutCommandForDev.getCartItemIds());
        // 3. CartItem => Item
        for (CheckoutItemForQueryProjection2 cartItem : cartItems) {
            String isbn = cartItem.getIsbn();
            int quantity = cartItem.getAmount();
            // 3.1 존재하는 Item인지 확인
            if (isbn == null || isbn.isEmpty()) {
                throw new BusinessLogicException(CheckoutErrorCode.INVALID_CART_ITEM);
            }
            // 3.2 재고 감소 로직
            try {
                optimisticLockStockFacade.decrease1(isbn, quantity);
            } catch (InterruptedException e) {
                // 3.3 정의하지 못한 오류
                Thread.currentThread().interrupt();
                throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, e);
            } catch (RuntimeException e) {
                // 3.4 동시성 문제 (Race Condition) 발생
                String errorMessage = e.getMessage() != null ? e.getMessage() : "";
                if (errorMessage.contains("현재 서버 부하로 결제 실패")) {
                    throw new BusinessLogicException(CheckoutErrorCode.CONCURRENCY_CONFLICT, e);
                }
                throw new BusinessLogicException(CheckoutErrorCode.STOCK_UNDERFLOW, e);
            }
        }
        // 4. PaymentEvent 생성 메서드

        PaymentEvent paymentEvent = createPaymentEvent(checkoutCommandForDev, cartItems);

        // 5. DB에 PaymentEvent 및 PaymentOrder를 저장 - Dirty Check
        try {
            paymentEventRepository.save(paymentEvent);
            return CheckoutResult.created(paymentEvent);
        } catch (DataIntegrityViolationException ex) {
            // 6. 중복성 체크
            if (isDuplicateKeyError(ex)) {
                // 6.1 이미 존재하는 주문인지 DB에서 확인
                Optional<PaymentCheckoutOptDtoForQueryProjection> exist =
                        checkoutFindExistingOrderService.findExistingOrder(paymentEvent.getOrderId());
                if (exist.isPresent()) {
                    // 6.2 이미 존재하는 주문이라면, 기존의 주문 정보를 반환
                    CheckoutResult existingResult = mapDtoToCheckoutResult(exist.get());
                    throw new DataConflictWithDataException(CheckoutErrorCode.ALREADY_PROCESSED_ORDER.getMessage(), existingResult);
                }
            }
            throw ex;
        } catch (Exception ex) {
            // 예상치 못한 오류, 500
            throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, ex);
        }
    }

    //Pessimistic
    @Transactional
    public CheckoutResult checkoutPessimisticLock(String userId, CheckoutRequest request) {

        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);

        List<CheckoutItemForQueryProjection2> cartItems =
                checkoutReadService.getCartItemsForEventCreation(checkoutCommandForDev.getCartItemIds());

        // 재고 확인
        for (CheckoutItemForQueryProjection2 cartItem : cartItems) {
            String isbn = cartItem.getIsbn();
            int quantity = cartItem.getAmount();

            if (isbn == null || isbn.isEmpty()) {
                throw new BusinessLogicException(CheckoutErrorCode.INVALID_CART_ITEM);
            }
            try {
                pessimisticLockItemService.decrease(isbn, quantity);
            } catch (PessimisticLockingFailureException e) {
                throw new BusinessLogicException(CheckoutErrorCode.CONCURRENCY_CONFLICT, e);
            } catch (RuntimeException e) {
                throw new BusinessLogicException(CheckoutErrorCode.STOCK_UNDERFLOW, e);
            }
        }
        PaymentEvent paymentEvent = createPaymentEvent(checkoutCommandForDev, cartItems);
        try {
            paymentEventRepository.save(paymentEvent);
            return CheckoutResult.created(paymentEvent);
        } catch (DataIntegrityViolationException ex) {
            if (isDuplicateKeyError(ex)) {
                Optional<PaymentCheckoutOptDtoForQueryProjection> exist =
                        checkoutFindExistingOrderService.findExistingOrder(paymentEvent.getOrderId());
                if (exist.isPresent()) {
                    CheckoutResult existingResult = mapDtoToCheckoutResult(exist.get());
                    throw new DataConflictWithDataException(CheckoutErrorCode.ALREADY_PROCESSED_ORDER.getMessage(), existingResult);
                }
            }
            throw ex;
        } catch (Exception ex) {

            throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, ex);
        }
    }


    /**
     * =====트랜잭션 분리 :: 현재 문제 : Lock Duration 3.496s==========================================================
     *
     * @ 트랜잭션 Scope 분리
     */

    @Transactional
    public CheckoutResult checkoutOptimisticLock_2(String userId, CheckoutRequest request) {
        // 1. Checkout 객체 생성
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);
        // 2. CartItem 조회 -> CartItem의 연관관계를 이용해서 Item Data를 가져온다.
        List<CheckoutItemForQueryProjection2> cartItems =
                checkoutReadService.getCartItemsForEventCreation(checkoutCommandForDev.getCartItemIds());

        // 3. CartItem => Item
        List<CheckoutItemForQueryProjection2> itemsToCompensate = decreaseItemQuantity(cartItems);
        // 4. PaymentEvent 생성 메서드
        PaymentEvent paymentEvent = createPaymentEvent(checkoutCommandForDev, cartItems);
        // 5. DB에 PaymentEvent 및 PaymentOrder를 저장 - Dirty Check
        try {
            paymentEventRepository.save(paymentEvent);
            return CheckoutResult.created(paymentEvent);
        } catch (DataIntegrityViolationException ex) {
            compensateStockIncreasePetVer(itemsToCompensate);
            // 6. 중복성 체크
            if (isDuplicateKeyError(ex)) {
                // 6.1 이미 존재하는 주문인지 DB에서 확인
                Optional<PaymentCheckoutOptDtoForQueryProjection> exist =
                        checkoutFindExistingOrderService.findExistingOrder(paymentEvent.getOrderId());
                if (exist.isPresent()) {
                    // 6.2 이미 존재하는 주문이라면, 기존의 주문 정보를 반환
                    CheckoutResult existingResult = mapDtoToCheckoutResult(exist.get());
//                    compensateStockIncrease(itemsToCompensate);
                    throw new DataConflictWithDataException(CheckoutErrorCode.ALREADY_PROCESSED_ORDER.getMessage(), existingResult);
                }
            }
            throw ex;
        } catch (Exception ex) {
            compensateStockIncreasePetVer(itemsToCompensate);
            // 예상치 못한 오류, 500
            throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, ex);
        }
    }


    /*
    public CheckoutResult checkoutOptimisticLock3(String userId, CheckoutRequest request){
        // 1. Checkout 객체 생성
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);
        // 2. CartItem 조회 -> CartItem의 연관관계를 이용해서 Item Data를 가져온다.
        List<CheckoutItemForQueryProjection2> cartItems =
                checkoutReadService.getCartItemsForEventCreation(checkoutCommandForDev.getCartItemIds());
        // 3. CartItem => Item
        decreaseItemQuantityProcess(cartItems);

        // 4. PaymentEvent 생성 메서드
        PaymentEvent paymentEvent = createPaymentEvent(checkoutCommandForDev,cartItems);
        // 5. DB에 PaymentEvent 및 PaymentOrder를 저장 - Dirty Check
        processPaymentSaveAsync(paymentEvent);

        // 6. 클라이언트에게 '접수 성공' 응답 즉시 반환

        // 실제 주문 ID는 비동기적으로 생성되므로, 임시 ID나 '처리 중' 상태를 반환

        return CheckoutResult.builder()
                .orderId(checkoutCommandForDev.getIdempotencyKey())
                .build();
    }

    // Item 재고 감소
    private void decreaseItemQuantityProcess(List<CheckoutItemForQueryProjection2> cartItems) {
        for (CheckoutItemForQueryProjection2 cartItem: cartItems){
            String isbn = cartItem.getIsbn();
            int quantity = cartItem.getAmount();
        // 3.1 존재하는 Item인지 확인
            if (isbn==null || isbn.isEmpty()){
                throw new BusinessLogicException(CheckoutErrorCode.INVALID_CART_ITEM);
            }
        // 3.2 재고 감소 로직
            try {
                // Facade 내부에서 Optimistic Lock Retry와 @Transaction 처리
                optimisticLockStockFacade.decrease(isbn,quantity);
            } catch (InterruptedException e){
        // 3.3 정의하지 못한 오류
                Thread.currentThread().interrupt();
                throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, e);
            } catch (RuntimeException e){
         // 3.4 동시성 문제 (Race Condition) 발생
                String errorMessage = e.getMessage() != null ? e.getMessage() : "";
                if (errorMessage.contains("현재 서버 부하로 결제 실패")) {
                    throw new BusinessLogicException(CheckoutErrorCode.CONCURRENCY_CONFLICT, e);
                }
                throw new BusinessLogicException(CheckoutErrorCode.STOCK_UNDERFLOW, e);
            }
        }
    }

    @Async("checkoutTaskExecutor") // AsyncConfig에서 정의한 스레드 풀 이름 사용
    public void processPaymentSaveAsync(PaymentEvent paymentEvent){
        // 1. 저장
        try {
            paymentEventRepository.save(paymentEvent);
        } catch (DataIntegrityViolationException ex){
        // 2. 중복성 체크
            if (isDuplicateKeyError(ex)){
                Optional<PaymentCheckoutOptDtoForQueryProjection> exist =
                        checkoutFindExistingOrderService.findExistingOrder(paymentEvent.getOrderId());
                if (exist.isPresent()){
                    // 이미 존재하는 주문이라면, 로그 기록 TODO
                    return;
                }
            }
            // 재고 원복 필요
            // increaseItem 로직
            throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, ex);
        } catch (Exception ex){
            // 재고 원복 필요
            // increaseItem 로직
            throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR,ex);
        }
    }
*/

    // Checkout 생성
    private static CheckoutCommandForDev getCheckoutCommand(String userId, CheckoutRequest request) {
        Long userIdLongType = Long.parseLong(userId);
        CheckoutCommandForDev checkoutCommandForDev = CheckoutCommandForDev.builder()
                .buyerId(userIdLongType)
                .cartItemIds(request.getCartItemIds())
                .idempotencyKey(IdempotencyCreator.create(request, userId))
                .build();

        return checkoutCommandForDev;
    }

    private List<CheckoutItemForQueryProjection2> decreaseItemQuantity(List<CheckoutItemForQueryProjection2> cartItems) {

        List<CheckoutItemForQueryProjection2> successfulItems = new ArrayList<>();

        for (CheckoutItemForQueryProjection2 cartItem : cartItems) {
            String isbn = cartItem.getIsbn();
            int quantity = cartItem.getAmount();
            // 3.1 존재하는 Item인지 확인
            if (isbn == null || isbn.isEmpty()) {
                throw new BusinessLogicException(CheckoutErrorCode.INVALID_CART_ITEM);
            }
            // 3.2 재고 감소 로직
            try {
                optimisticLockStockFacade.decrease1(isbn, quantity);
                successfulItems.add(cartItem);
            } catch (InterruptedException e) {
                // 3.3 정의하지 못한 오류
                Thread.currentThread().interrupt();
                throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, e);
            } catch (RuntimeException e) {
                // 3.4 동시성 문제 (Race Condition) 발생
                String errorMessage = e.getMessage() != null ? e.getMessage() : "";
                if (errorMessage.contains("현재 서버 부하로 결제 실패")) {
                    throw new BusinessLogicException(CheckoutErrorCode.CONCURRENCY_CONFLICT, e);
                }
                throw new BusinessLogicException(CheckoutErrorCode.STOCK_UNDERFLOW, e);
            }
        }
        return successfulItems;
    }

    private boolean isDuplicateKeyError(DataIntegrityViolationException ex) {
        Throwable cause = ex.getRootCause();
        return (cause instanceof SQLIntegrityConstraintViolationException);
    }

    private CheckoutResult mapDtoToCheckoutResult(PaymentCheckoutOptDtoForQueryProjection dto) {
        return CheckoutResult.alreadyExists2(dto);
    }


    private PaymentEvent createPaymentEvent(
            CheckoutCommandForDev command,
            List<CheckoutItemForQueryProjection2> cartItems
    ) {
        List<Long> cartItemIds = command.getCartItemIds();

        AccountEntity account = cartItems.getFirst().getAccountEntity();

        //PaymentOrder
        List<PaymentOrder> paymentOrders = cartItems.stream()
                .map(cartItem -> PaymentOrder.builder()
                        .sellerId(cartItem.getSellerId())
                        .orderId(command.getIdempotencyKey())
                        .productId(cartItem.getIsbn())
                        .amount(cartItem.getPrice() * cartItem.getAmount())
                        .paymentStatus(PaymentStatus.NOT_STARTED)
                        .build()
                )
                .collect(Collectors.toList());

        // 주문 이름
        String orderName = cartItems.stream()
                .map(cartItem -> cartItem.getTitle())
                .collect(Collectors.joining(", "));

        PaymentEvent build = PaymentEvent.builder()
                .accountEntity(account)
                .orderId(command.getIdempotencyKey())
                .orderName(orderName)
                .paymentOrders(paymentOrders)
                .build();

        for (PaymentOrder order : paymentOrders) {
            order.setPaymentEvent(build);
        }

        return build;
    }

    private void compensateStockIncreaseOptVer(List<CheckoutItemForQueryProjection2> itemsToCompensate) {
        // 복구할 아이템이 없는 경우 -> 종료
        if (itemsToCompensate.isEmpty()) {
            return;
        }

        // 로그를 남겨 재고 복구 작업이 이루어졌음을 명확히 기록
        log.warn("🚨 보상 트랜잭션 시작: 메인 트랜잭션 실패로 인해 {}개의 아이템 재고 복구 시도.", itemsToCompensate.size());

        for (CheckoutItemForQueryProjection2 item : itemsToCompensate) {
            try {
                // ⭐ [수정] 재시도 로직이 포함된 Facade 메서드를 호출
                // increase1 메서드는 내부적으로 락 충돌 시 5회 재시도 로직을 수행합니다.
                optimisticLockStockFacade.increase1(item.getIsbn(), item.getAmount());

                log.info("✅ 재고 복구 성공: ISBN={}, 수량={}", item.getIsbn(), item.getAmount());

            } catch (InterruptedException e) {
                // Thread.sleep 중 발생한 인터럽트 처리 (치명적)
                Thread.currentThread().interrupt();
                log.error(" 치명적 오류: 재고 복구 중 인터럽트 발생. 수동 확인 필요. ISBN={}", item.getIsbn(), e);
            } catch (RuntimeException e) {
                // increase1 내부에서 5회 재시도 후 최종적으로 던져진 예외를 여기서 처리
                // Lock Wait Timeout 또는 Optimistic Lock Failure가 최종적으로 여기서 포착됨
                log.error(" 치명적 오류: 재고 복구 트랜잭션 최종 실패. 수동 확인 필요. ISBN={}", item.getIsbn(), e);

                // 중요: 이 오류는 데이터 불일치를 의미하므로, 모니터링/알람 시스템으로 전달해야 합니다.
            }
        }
    }
    private void compensateStockIncreasePetVer(List<CheckoutItemForQueryProjection2> itemsToCompensate) {
        // 복구할 아이템이 없는 경우 -> 종료
        if (itemsToCompensate.isEmpty()) {
            return;
        }

        // 로그를 남겨 재고 복구 작업이 이루어졌음을 명확히 기록
        log.warn("🚨 보상 트랜잭션 시작: 메인 트랜잭션 실패로 인해 {}개의 아이템 재고 복구 시도.", itemsToCompensate.size());

        for (CheckoutItemForQueryProjection2 item : itemsToCompensate) {
            try {
                // ⭐ [수정] 재시도 로직이 포함된 Facade 메서드를 호출
                // increase1 메서드는 내부적으로 락 충돌 시 5회 재시도 로직을 수행합니다.
                pessimisticLockStockFacade.increase_1(item.getIsbn(), item.getAmount());

                log.info("✅ 재고 복구 성공: ISBN={}, 수량={}", item.getIsbn(), item.getAmount());

            }  catch (RuntimeException e) {
                // increase1 내부에서 5회 재시도 후 최종적으로 던져진 예외를 여기서 처리
                // Lock Wait Timeout 또는 Optimistic Lock Failure가 최종적으로 여기서 포착됨
                log.error(" 치명적 오류: 재고 복구 트랜잭션 최종 실패. 수동 확인 필요. ISBN={}", item.getIsbn(), e);

                // 중요: 이 오류는 데이터 불일치를 의미하므로, 모니터링/알람 시스템으로 전달해야 합니다.
            }
        }
    }
}