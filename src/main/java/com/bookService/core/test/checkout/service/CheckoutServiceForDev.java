package com.bookService.core.test.checkout.service;

import com.bookService.core.common.exception.checkout.BusinessLogicException;
import com.bookService.core.common.exception.checkout.CheckoutErrorCode;
import com.bookService.core.common.exception.checkout.CheckoutException;
import com.bookService.core.common.exception.checkout.DataConflictWithDataException;
import com.bookService.core.common.util.IdempotencyCreator;
import com.bookService.core.domain.cartitem.CartItem;
import com.bookService.core.domain.cartitem.dto.CheckoutItemForQueryProjection;
import com.bookService.core.domain.cartitem.dto.CheckoutItemForQueryProjection2;
import com.bookService.core.domain.cartitem.repository.CartItemRepository;
import com.bookService.core.domain.checkout.dto.CheckoutCommandForDev;
import com.bookService.core.domain.checkout.dto.CheckoutRequest;
import com.bookService.core.domain.checkout.dto.CheckoutResult;
import com.bookService.core.domain.login.entity.AccountEntity;
import com.bookService.core.facade.OptimisticLockStockFacade;
import com.bookService.core.test.checkout.repository.CheckoutTransactionRepository;
import com.bookService.core.domain.payment.dto.PaymentCheckoutOptDtoForQueryProjection;
import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import com.bookService.core.domain.payment.entity.PaymentEvent;
import com.bookService.core.domain.payment.entity.PaymentOrder;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentEventRepository;
import com.bookService.core.domain.payment.usecase.CheckoutFindExistingOrderUseCase;
import com.bookService.core.test.item.PessimisticLockItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutServiceForDev  {

    private final SpringDataJpaPaymentEventRepository paymentEventRepository;
    private final CartItemRepository cartItemRepository;
    private final CheckoutFindExistingOrderUseCase checkoutFindExistingOrderService;
    private final CheckoutReadService checkoutReadService;
    private final CheckoutTransactionRepository transactionRepository;

    private final OptimisticLockStockFacade optimisticLockStockFacade;

    private final PessimisticLockItemService pessimisticLockItemService;
    /** //==Ver1. Origin==// */
    @Transactional
    public CheckoutResult checkout1(String userId, CheckoutRequest request) {
        CheckoutCommandForDev command = getCheckoutCommand(userId, request);

        // 1. 중복 주문을 방지하기 위해, PaymentEvent가 있는지 사전 조회
        Optional<PaymentEvent> paymentEventOptional = paymentEventRepository.findByOrderId(command.getIdempotencyKey());

        if (paymentEventOptional.isPresent()){
            PaymentEvent paymentEvent = paymentEventOptional.get();
            return new CheckoutResult(
                    paymentEvent.totalAmount(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getOrderName());
        }

        // 2. 새 PaymentEvent 생성 & 저장
        PaymentEvent paymentEvent = createPaymentEventBeforeRefactoring(command);

            paymentEventRepository.save(paymentEvent);
            return CheckoutResult.created(paymentEvent);
    }
    /** //==Ver2. checkout의 인자를  CheckoutCommand에서 userid, request롤 받고, 메서드 내에서 CheckoutCommand 생성 -> Controller에서 userId를 통해 Cart 호출X ==// */
    @Transactional
    public CheckoutResult checkout2(String userId, CheckoutRequest request) {
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);

        // 1. 중복 주문을 방지하기 위해, PaymentEvent가 있는지 사전 조회
        Optional<PaymentEvent> paymentEventOptional = paymentEventRepository.findByOrderId(checkoutCommandForDev.getIdempotencyKey());

        if (paymentEventOptional.isPresent()){
            PaymentEvent paymentEvent = paymentEventOptional.get();
            return new CheckoutResult(
                    paymentEvent.totalAmount(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getOrderName());
        }

        // 2. 새 PaymentEvent 생성 & 저장
        PaymentEvent paymentEvent = createPaymentEvent1(checkoutCommandForDev);
        paymentEventRepository.save(paymentEvent);
        return new CheckoutResult(
                paymentEvent.totalAmount(),
                paymentEvent.getOrderId(),
                paymentEvent.getOrderName()
        );

    }
    /** //==Ver3 createPayment2 도입 :: CartItem 및 Item 조회 시 Projection으로 불필요한 데이터 호출 방지 ==// */
    @Transactional
    public CheckoutResult checkout3(String userId, CheckoutRequest request) {
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);

        Optional<PaymentEvent> paymentEventOptional = paymentEventRepository.findByOrderId(checkoutCommandForDev.getIdempotencyKey());

        if (paymentEventOptional.isPresent()){
            PaymentEvent paymentEvent = paymentEventOptional.get();
            return new CheckoutResult(
                    paymentEvent.totalAmount(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getOrderName());
        }

        PaymentEvent paymentEvent = createPaymentEvent2(checkoutCommandForDev);

        paymentEventRepository.save(paymentEvent);
        return new CheckoutResult(
                paymentEvent.totalAmount(),
                paymentEvent.getOrderId(),
                paymentEvent.getOrderName()
        );
    }
    
    /** //==Ver4 findPaymentOptByOrderID 도입 :: 중복 상황시 호출하는 paymentEvent 조회를 Projection으로 불필요한 데이터 호출 방지  ==// */
    @Transactional
    public CheckoutResult checkout4(String userId, CheckoutRequest request) {
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);

        // 1. 중복 주문을 방지하기 위해, PaymentEvent가 있는지 사전 조회
        Optional<PaymentCheckoutOptDtoForQueryProjection> paymentEventOptional = paymentEventRepository.findPaymentOptByOrderID(checkoutCommandForDev.getIdempotencyKey());

        if (paymentEventOptional.isPresent()){
            PaymentCheckoutOptDtoForQueryProjection paymentEvent = paymentEventOptional.get();

            return new CheckoutResult(
                    paymentEvent.getTotalAmount(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getOrderName());
        } else  {
            // 2. 새 PaymentEvent 생성 & 저장
            PaymentEvent paymentEvent = createPaymentEvent2(checkoutCommandForDev);

            paymentEventRepository.save(paymentEvent);
            return new CheckoutResult(
                    paymentEvent.totalAmount(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getOrderName()
            );
        }

    }

    @Transactional
    public CheckoutResult checkout5(String userId, CheckoutRequest request) {
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);

        // 1. 중복 주문을 방지하기 위해, PaymentEvent가 있는지 사전 조회
        Optional<PaymentCheckoutOptDtoForQueryProjection> paymentEventOptional = paymentEventRepository.findPaymentOptByOrderID(checkoutCommandForDev.getIdempotencyKey());

        if (paymentEventOptional.isPresent()){
            PaymentCheckoutOptDtoForQueryProjection paymentEvent = paymentEventOptional.get();

            return new CheckoutResult(
                    paymentEvent.getTotalAmount(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getOrderName());
        } else  {
            // 2. 새 PaymentEvent 생성 & 저장
            PaymentEvent paymentEvent = createPaymentEvent3(checkoutCommandForDev);

            paymentEventRepository.save(paymentEvent);
            return new CheckoutResult(
                    paymentEvent.totalAmount(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getOrderName()
            );
        }

    }


    /** //==Ver6-1 전통적 try-catch  ==// */
    @Transactional
    public CheckoutResult checkout6_1(String userId, CheckoutRequest request) {
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);

        try {
            PaymentEvent paymentEvent = createPaymentEvent2(checkoutCommandForDev);
            PaymentEvent saved = paymentEventRepository.save(paymentEvent);

            return new CheckoutResult(
                    paymentEvent.totalAmount(),
                    paymentEvent.getOrderId(),
                    paymentEvent.getOrderName()
            );
        } catch (DataIntegrityViolationException e){

            String idempotencyKey = checkoutCommandForDev.getIdempotencyKey();

            Optional<PaymentCheckoutOptDtoForQueryProjection> existingOrder = checkoutFindExistingOrderService.findExistingOrder(idempotencyKey);

            if (existingOrder.isPresent()){
                PaymentCheckoutOptDtoForQueryProjection paymentEvent = existingOrder.get();
                return new CheckoutResult(
                        paymentEvent.getTotalAmount(),
                        paymentEvent.getOrderId(),
                        paymentEvent.getOrderName()
                        );
            }
        throw e;
        }
    }
    /** //==Ver6-2 6-1의 경우, 1.실패한 paymentEvent엔티티가, 영속성 컨텍스트에 여전히 남아있다.
     *                        2. checkout 삽입 실패 후 예외 발생 후, 트랜잭션 롤백으로 실패==// */
    @Transactional(noRollbackFor = DataIntegrityViolationException.class)
    public CheckoutResult checkout6_2(CheckoutCommandForDev checkoutCommandForDev) {

        PaymentEvent paymentEvent = createPaymentEvent2(checkoutCommandForDev);
        paymentEventRepository.save(paymentEvent);
        return new CheckoutResult(
                paymentEvent.totalAmount(),
                paymentEvent.getOrderId(),
                paymentEvent.getOrderName()
        );
    }

    public CheckoutResult checkout6_3(String userId, CheckoutRequest request) {
        PaymentEvent paymentEvent = getCheckoutCommandAndMakePaymentEvent(userId, request);

        Optional<PaymentCheckoutOptDtoForQueryProjection> checkoutOrSelectExistingPayment
                = transactionRepository.createCheckoutOrSelectExistingPayment(paymentEvent);
        if(checkoutOrSelectExistingPayment.isPresent()){
            PaymentCheckoutOptDtoForQueryProjection paymentCheckoutOptDtoForQueryProjection = checkoutOrSelectExistingPayment.get();
            return new CheckoutResult(
                    paymentCheckoutOptDtoForQueryProjection.getTotalAmount(),
                    paymentCheckoutOptDtoForQueryProjection.getOrderId(),
                    paymentCheckoutOptDtoForQueryProjection.getOrderName());
        }
        return new CheckoutResult(paymentEvent.totalAmount(),
                paymentEvent.getOrderId(),
                paymentEvent.getOrderName());
    }

    //  오류 캐치 후 재시도 -> Unique Key 데이터 정합성 오류 발생 후, catch 한 다음 반환을 해보려고 해도 실패하고 있다.
    public CheckoutResult checkout6_4(String userId, CheckoutRequest request) throws CheckoutException {

        PaymentEvent paymentEvent = getCheckoutCommandAndMakePaymentEvent(userId, request);

        try {
            log.info("1. 결제 이벤트 저장 시도 orderId: {}", paymentEvent.getOrderId());
            paymentEventRepository.save(paymentEvent);
            log.info("저장 성공");
            return CheckoutResult.builder()
                    .amount(paymentEvent.totalAmount())
                    .orderId(paymentEvent.getOrderId())
                    .orderName(paymentEvent.getOrderName())
                    .build();

        }catch (DataIntegrityViolationException e){

            log.info("------------무슨 오류인지 확인이나 해보자 STACK Trace: {} ", (Object) e.getStackTrace());
            log.info("------------무슨 오류인지 확인이나 해보자 STACK Trace: {} ", (Object) e.getCause());


            log.info("중복키 오류 발생 orderId: {}",paymentEvent.getOrderId());

            log.info("재시도 로직을 활용하여, 기존의 주문을 조회 후 반환");

            Optional<PaymentCheckoutOptDtoForQueryProjection> existingOrder = Optional.empty();

            int maxRetries = 2;
            long retryDelayMillis = 2000; // 100ms 대기
            for (int i = 0; i<maxRetries; i++){
                log.debug(" 기존 Payment 반환 시도 : {} - Retry{}/{}",paymentEvent.getOrderId(), i+1, maxRetries);
                existingOrder = checkoutFindExistingOrderService.findExistingOrder(paymentEvent.getOrderId());

                if(existingOrder.isPresent()){
                    log.info("======================반복 횟수================={}, orderID: {}",i+1,paymentEvent.getOrderId());
                    break;
                }
                if (i<maxRetries - 1){
                    try {
                        log.debug("찾지 못한 횟수 {}. 대기 시간 {}ms 전 시도", i+1, retryDelayMillis);
                        Thread.sleep(retryDelayMillis);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        log.error("쓰레드 interrupted ", ex);

                        throw new CheckoutException("Thread was interrupted during retry wait.", ex);
                    }
                }
            }

            log.error("==========================재시도 이후 실패");
            PaymentCheckoutOptDtoForQueryProjection existingOrderProjection = existingOrder
                    .orElseThrow(() -> new CheckoutException("Existing order not found after multiple retries for a duplicate key collision. This indicates a potential data inconsistency or timing issue."));

            return CheckoutResult.builder()
                    .amount(existingOrderProjection.getTotalAmount())
                    .orderId(existingOrderProjection.getOrderId())
                    .orderName(existingOrderProjection.getOrderName())
                    .build();


        } catch (Exception e) {
            // 다른 종류의 예외가 발생하면, 트랜잭션을 롤백하고 재처리할 수 있도록 예외를 던집니다.
            log.error("An unexpected error occurred during checkout.", e);
            throw new CheckoutException("An unexpected error occurred.", e);
        }
    }

    @Transactional
    public CheckoutResult checkout6_5(CheckoutCommandForDev command) {
        PaymentEvent paymentEvent = createPaymentEvent2(command);
        paymentEventRepository.save(paymentEvent);
        CheckoutResult result = CheckoutResult.builder()
                .amount(paymentEvent.totalAmount())
                .orderId(paymentEvent.getOrderId())
                .orderName(paymentEvent.getOrderName())
                .build();
        return result;
    }

    public CheckoutResult checkout7(String userId, CheckoutRequest request) throws CheckoutException {
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);
        PaymentEvent paymentEvent = createPaymentEvent2(checkoutCommandForDev);
        try{
            paymentEventRepository.save(paymentEvent);
            return CheckoutResult.created(paymentEvent);
        } catch (DataIntegrityViolationException ex) {
            if (isDuplicateKeyError(ex)) {

                Optional<PaymentCheckoutOptDtoForQueryProjection> exist =
                        checkoutFindExistingOrderService.findExistingOrder(paymentEvent.getOrderId());

                return CheckoutResult.alreadyExists(paymentEvent);
            }
            throw new CheckoutException("DB 오류", ex);
        }
    }

    public CheckoutResult checkout8(String userId, CheckoutRequest request) throws CheckoutException {
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);
        PaymentEvent paymentEvent = createPaymentEvent3(checkoutCommandForDev);
        try{
            paymentEventRepository.save(paymentEvent);
            return CheckoutResult.created(paymentEvent);
        } catch (DataIntegrityViolationException ex) {
            // 409 중복 키 오류 처리
            if (isDuplicateKeyError(ex)) {

                Optional<PaymentCheckoutOptDtoForQueryProjection> exist =
                        checkoutFindExistingOrderService.findExistingOrder(paymentEvent.getOrderId());

                return CheckoutResult.alreadyExists(paymentEvent);
            }
            throw new CheckoutException("DB 오류", ex);
        } catch (ObjectOptimisticLockingFailureException ex){
            // 3. (재고/락킹 로직이 있다면) Optimistic Lock 실패 처리
            // throw new CheckoutException("동시성 충돌, 재시도 필요", ex); // HTTP 409 또는 503
        } catch (Exception ex) {
            // 4. 기타 예상치 못한 모든 오류 처리 (General 500)
            // log.error("예상치 못한 결제 처리 오류", ex);
            return CheckoutResult.failed();        }
        return null;
    }
    /** ======================================예외 템플릿================================================================*/
    /** createPaymentEventBeforeRefactoring */
    public CheckoutResult checkout9_1(String userId, CheckoutRequest request) {
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);
        // 1차 - createPayment 비교
        PaymentEvent paymentEvent = createPaymentEventBeforeRefactoring(checkoutCommandForDev);
        try{
            paymentEventRepository.save(paymentEvent);
            // 성공 시, SUCCESS 상태의 CheckoutResult 반환
            return CheckoutResult.created(paymentEvent);
        } catch (DataIntegrityViolationException ex) {
            // 중복 키 오류 (409 Conflict) 발생
            if (isDuplicateKeyError(ex)) {
                // 기존 데이터 조회 성공 시, 데이터를 담아서 DataConflictWithDataException를 던진다.
                Optional<PaymentCheckoutOptDtoForQueryProjection> exist =
                        checkoutFindExistingOrderService.findExistingOrder(paymentEvent.getOrderId());
                if (exist.isPresent()) {
                    // 2. CheckoutResult를 생성합니다.
                    CheckoutResult existingResult = mapDtoToCheckoutResult(exist.get());

                    // 3. 데이터를 담아 Custom Exception을 던지면, GlobalExceptionHandler의
                    //    handleDataConflictWithDataException 이 이를 409로 처리
                    throw new DataConflictWithDataException("이미 처리된 주문입니다.", existingResult);
                }
            }
            // 중복 키가 아닌 다른 형태의 DataIntegrityViolationException이 발생하면
            // GlobalExceptionHandler의 DataIntegrityViolationException 핸들러(500)로 던진다.
            throw ex;
        } catch (Exception ex) {
            throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, ex);
        }
    }
    /** createPaymentEvent */
    public CheckoutResult checkout9_2(String userId, CheckoutRequest request) {
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);
        PaymentEvent paymentEvent = createPaymentEvent1(checkoutCommandForDev);
        try{
            paymentEventRepository.save(paymentEvent);
            // 성공 시, SUCCESS 상태의 CheckoutResult 반환
            return CheckoutResult.created(paymentEvent);
        } catch (DataIntegrityViolationException ex) {
            // 중복 키 오류 (409 Conflict) 발생
            if (isDuplicateKeyError(ex)) {
                // 기존 데이터 조회 성공 시, 데이터를 담아서 DataConflictWithDataException를 던진다.
                Optional<PaymentCheckoutOptDtoForQueryProjection> exist =
                        checkoutFindExistingOrderService.findExistingOrder(paymentEvent.getOrderId());

                if (exist.isPresent()) {
                    // 2. CheckoutResult를 생성합니다.
                    CheckoutResult existingResult = mapDtoToCheckoutResult(exist.get());

                    // 3. 데이터를 담아 Custom Exception을 던지면, GlobalExceptionHandler의
                    //    handleDataConflictWithDataException 이 이를 409로 처리
                    throw new DataConflictWithDataException("이미 처리된 주문입니다.", existingResult);
                }
            }
            // 중복 키가 아닌 다른 형태의 DataIntegrityViolationException이 발생하면
            // GlobalExceptionHandler의 DataIntegrityViolationException 핸들러(500)로 던진다.
            throw ex;
        } catch (Exception ex) {
            throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, ex);
        }
    }
    /** createPaymentEvent2 */
    public CheckoutResult checkout9_3(String userId, CheckoutRequest request) {
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);
        PaymentEvent paymentEvent = createPaymentEvent3(checkoutCommandForDev);
        try{
            paymentEventRepository.save(paymentEvent);
            // 성공 시, SUCCESS 상태의 CheckoutResult 반환
            return CheckoutResult.created(paymentEvent);
        } catch (DataIntegrityViolationException ex) {
            // 중복 키 오류 (409 Conflict) 발생
            if (isDuplicateKeyError(ex)) {
                // 기존 데이터 조회 성공 시, 데이터를 담아서 DataConflictWithDataException를 던진다.
                Optional<PaymentCheckoutOptDtoForQueryProjection> exist =
                        checkoutFindExistingOrderService.findExistingOrder(paymentEvent.getOrderId());

                if (exist.isPresent()) {
                    // 2. CheckoutResult를 생성합니다.
                    CheckoutResult existingResult = mapDtoToCheckoutResult(exist.get());

                    // 3. 데이터를 담아 Custom Exception을 던지면, GlobalExceptionHandler의
                    //    handleDataConflictWithDataException 이 이를 409로 처리
                    throw new DataConflictWithDataException("이미 처리된 주문입니다.", existingResult);
                }
            }
            // 중복 키가 아닌 다른 형태의 DataIntegrityViolationException이 발생하면
            // GlobalExceptionHandler의 DataIntegrityViolationException 핸들러(500)로 던진다.
            throw ex;
        } catch (Exception ex) {
            throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, ex);        }
    }
    /** createPaymentEvent3 */
    public CheckoutResult checkout9_4(String userId, CheckoutRequest request) {
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);
        PaymentEvent paymentEvent = createPaymentEvent3(checkoutCommandForDev);
        try{
            paymentEventRepository.save(paymentEvent);
            // 성공 시, SUCCESS 상태의 CheckoutResult 반환
            return CheckoutResult.created(paymentEvent);
        } catch (DataIntegrityViolationException ex) {
            // 중복 키 오류 (409 Conflict) 발생
            if (isDuplicateKeyError(ex)) {
                // 기존 데이터 조회 성공 시, 데이터를 담아서 DataConflictWithDataException를 던진다.
                Optional<PaymentCheckoutOptDtoForQueryProjection> exist =
                        checkoutFindExistingOrderService.findExistingOrder(paymentEvent.getOrderId());

                if (exist.isPresent()) {
                    // 2. CheckoutResult를 생성합니다.
                    CheckoutResult existingResult = mapDtoToCheckoutResult(exist.get());

                    // 3. 데이터를 담아 Custom Exception을 던지면, GlobalExceptionHandler의
                    //    handleDataConflictWithDataException 이 이를 409로 처리
                    throw new DataConflictWithDataException("이미 처리된 주문입니다.", existingResult);
                }
            }
            // 중복 키가 아닌 다른 형태의 DataIntegrityViolationException이 발생하면
            // GlobalExceptionHandler의 DataIntegrityViolationException 핸들러(500)로 던진다.
            throw ex;
        } catch (Exception ex) {
            throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, ex);
        }
    }

    public CheckoutResult checkout9_5(String userId, CheckoutRequest request){
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);
        List<CheckoutItemForQueryProjection2> cartItems =
                checkoutReadService.getCartItemsForEventCreation(checkoutCommandForDev.getCartItemIds());
        PaymentEvent paymentEvent = createPaymentEvent4(checkoutCommandForDev,cartItems);
        try{
            paymentEventRepository.save(paymentEvent);
            // 성공 시, SUCCESS 상태의 CheckoutResult 반환
            return CheckoutResult.created(paymentEvent);
        } catch (DataIntegrityViolationException ex) {
            // 중복 키 오류 (409 Conflict) 발생
            if (isDuplicateKeyError(ex)) {
                // 기존 데이터 조회 성공 시, 데이터를 담아서 DataConflictWithDataException를 던진다.
                Optional<PaymentCheckoutOptDtoForQueryProjection> exist =
                        checkoutFindExistingOrderService.findExistingOrder(paymentEvent.getOrderId());

                if (exist.isPresent()) {
                    // 2. CheckoutResult를 생성합니다.
                    CheckoutResult existingResult = mapDtoToCheckoutResult(exist.get());

                    // 3. 데이터를 담아 Custom Exception을 던지면, GlobalExceptionHandler의
                    //    handleDataConflictWithDataException 이 이를 409로 처리
                    throw new DataConflictWithDataException("이미 처리된 주문입니다.", existingResult);
                }
            }
            // 중복 키가 아닌 다른 형태의 DataIntegrityViolationException이 발생하면
            // GlobalExceptionHandler의 DataIntegrityViolationException 핸들러(500)로 던진다.
            throw ex;
        } catch (Exception ex) {
            throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, ex);
        }
    }

    // OptimisticLockVer
    @Transactional
    public CheckoutResult checkout10_OptimisticLock(String userId, CheckoutRequest request){
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);
        List<CheckoutItemForQueryProjection2> cartItems =
                checkoutReadService.getCartItemsForEventCreation(checkoutCommandForDev.getCartItemIds());
        for (CheckoutItemForQueryProjection2 cartItem: cartItems){
            String isbn = cartItem.getIsbn();
            int quantity = cartItem.getAmount();
            if (isbn==null || isbn.isEmpty()){
                throw new BusinessLogicException(CheckoutErrorCode.INVALID_CART_ITEM);
            }
            try {
                optimisticLockStockFacade.decrease1(isbn,quantity);
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
                throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, e);
            } catch (RuntimeException e){
                String errorMessage = e.getMessage() != null ? e.getMessage() : "";
                if (errorMessage.contains("현재 서버 부하로 결제 실패")) {
                    throw new BusinessLogicException(CheckoutErrorCode.CONCURRENCY_CONFLICT, e);
                }
                throw new BusinessLogicException(CheckoutErrorCode.STOCK_UNDERFLOW, e);
            }
        }
        PaymentEvent paymentEvent = createPaymentEvent4(checkoutCommandForDev,cartItems);
        try{
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
            // 예상치 못한 오류, 500
            throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, ex);
        }
    }

    //Pessimistic
    @Transactional
    public CheckoutResult checkout10_PessimisticLock(String userId, CheckoutRequest request){

        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);

        List<CheckoutItemForQueryProjection2> cartItems =
                checkoutReadService.getCartItemsForEventCreation(checkoutCommandForDev.getCartItemIds());

        // 재고 확인
        for (CheckoutItemForQueryProjection2 cartItem: cartItems){
            String isbn = cartItem.getIsbn();
            int quantity = cartItem.getAmount();

            if (isbn==null || isbn.isEmpty()){
                throw new BusinessLogicException(CheckoutErrorCode.INVALID_CART_ITEM);
            }
            try {
                pessimisticLockItemService.decrease(isbn,quantity);
            } catch (PessimisticLockingFailureException e){
                throw new BusinessLogicException(CheckoutErrorCode.CONCURRENCY_CONFLICT, e);
            } catch (RuntimeException e){
                throw new BusinessLogicException(CheckoutErrorCode.STOCK_UNDERFLOW, e);
            }
        }
        PaymentEvent paymentEvent = createPaymentEvent4(checkoutCommandForDev,cartItems);
        try{
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



    private boolean isDuplicateKeyError(DataIntegrityViolationException ex) {
        Throwable cause = ex.getRootCause();
        return (cause instanceof SQLIntegrityConstraintViolationException);
    }

    private CheckoutResult mapDtoToCheckoutResult(PaymentCheckoutOptDtoForQueryProjection dto) {
        // 기존 로직을 참고하여 CheckoutResult를 반환하도록 구현
        return CheckoutResult.alreadyExists2(dto);
    }


    private PaymentEvent getCheckoutCommandAndMakePaymentEvent(String userId, CheckoutRequest request) {
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);
        PaymentEvent paymentEvent = createPaymentEvent2(checkoutCommandForDev);
        return paymentEvent;
    }


    private static CheckoutCommandForDev getCheckoutCommand(String userId, CheckoutRequest request) {
        Long userIdLongType = Long.parseLong(userId);
        CheckoutCommandForDev checkoutCommandForDev = CheckoutCommandForDev.builder()
                .buyerId(userIdLongType)
                .cartItemIds(request.getCartItemIds())
                // order_id로 사용될 멱등성 키를 생성합니다.
                .idempotencyKey(IdempotencyCreator.create(request, userId))
                .build();

        return checkoutCommandForDev;
    }

    // N+1 유발
    private PaymentEvent createPaymentEventBeforeRefactoring(CheckoutCommandForDev command){
        List<Long> cartItemIds = command.getCartItemIds();

        List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);

        AccountEntity account = cartItems.getFirst().getCart().getAccount();

        List<PaymentOrder> paymentOrders = cartItems.stream()
                .map(cartItem -> PaymentOrder.builder()
                        .sellerId(cartItem.getItem().getSellerId())
                        .orderId(command.getIdempotencyKey())
                        .productId(cartItem.getItem().getIsbn())
                        .amount(cartItem.getItem().getPrice() * cartItem.getAmount())
                        .paymentStatus(PaymentStatus.NOT_STARTED)
                        .build()
                )
                .collect(Collectors.toList());

        // 주문 이름
        String orderName = cartItems.stream()
                .map(cartItem -> cartItem.getItem().getTitle())
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

    /** //==Ver1. Origin==// */
    private PaymentEvent createPaymentEvent1(CheckoutCommandForDev command) {
        List<Long> cartItemIds = command.getCartItemIds();
        List<CartItem> cartItems = cartItemRepository.findAllWithItemByIdIn(cartItemIds);

        AccountEntity account = cartItems.getFirst().getCart().getAccount();

        List<PaymentOrder> paymentOrders = cartItems.stream()
                .map(cartItem -> PaymentOrder.builder()
                        .sellerId(cartItem.getItem().getSellerId())
                        .orderId(command.getIdempotencyKey())
                        .productId(cartItem.getItem().getIsbn())
                        .amount(cartItem.getItem().getPrice() * cartItem.getAmount())
                        .paymentStatus(PaymentStatus.NOT_STARTED)
                        .build()
                )
                .collect(Collectors.toList());

        // 주문 이름
        String orderName = cartItems.stream()
                .map(cartItem -> cartItem.getItem().getTitle())
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
    /** //== customCartItemProjection::  CartItem 및 Item 조회 시 Projection으로 불필요한 데이터 호출 방지 ==// */
    private PaymentEvent createPaymentEvent2(CheckoutCommandForDev command)  {
        List<Long> cartItemIds = command.getCartItemIds();
        List<CheckoutItemForQueryProjection> cartItems = cartItemRepository.customCartItemProjection(cartItemIds);
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
                .buyerId(command.getBuyerId())
                .orderId(command.getIdempotencyKey())
                .orderName(orderName)
                .paymentOrders(paymentOrders)
                .build();

        for (PaymentOrder order : paymentOrders) {
            order.setPaymentEvent(build);
        }

        return build;
    }

    private PaymentEvent createPaymentEvent3(CheckoutCommandForDev command)  {
        List<Long> cartItemIds = command.getCartItemIds();
        List<CheckoutItemForQueryProjection2> cartItems = cartItemRepository.customCartItemProjection2(cartItemIds);

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

    private PaymentEvent createPaymentEvent4(
            CheckoutCommandForDev command,
            List<CheckoutItemForQueryProjection2> cartItems
            )  {
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

    /** ===================================@Async=================================*/
    @Transactional
    public CheckoutResult checkout10_OptimisticLock_Async(String userId, CheckoutRequest request) {
        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);
        List<CheckoutItemForQueryProjection2> cartItems =
                checkoutReadService.getCartItemsForEventCreation(checkoutCommandForDev.getCartItemIds());

        // 1. 비동기 작업들을 담을 리스트 선언
        List<CompletableFuture<Void>> stockFutures = new ArrayList<>();

        // 복구 시 사용할 품목 정보 저장
        List<CheckoutItemForQueryProjection2> itemsToCompensate = new ArrayList<>();

        for (CheckoutItemForQueryProjection2 cartItem : cartItems) {
            String isbn = cartItem.getIsbn();
            int quantity = cartItem.getAmount();
            if (isbn == null || isbn.isEmpty()) {
                throw new BusinessLogicException(CheckoutErrorCode.INVALID_CART_ITEM);
            }

            // 2. @Async 재고 감소 메서드를 호출하여 Future를 리스트에 수집
            stockFutures.add(optimisticLockStockFacade.decreaseAsync(isbn, quantity));
            // 보상 목록에 추가 (재고 감소 시도 대상 목록)
            itemsToCompensate.add(cartItem);
        }

        // 결제 이벤트는 재고 감소 로직 완료 후에 생성되어야 함
        PaymentEvent paymentEvent = createPaymentEvent4(checkoutCommandForDev, cartItems);

        try {
            // A. 재고 감소 완료 대기 및 비동기 예외 처리
            CompletableFuture.allOf(stockFutures.toArray(new CompletableFuture[0])).join();

            // B. 결제 이벤트 저장 (메인 트랜잭션의 핵심 커밋)
            paymentEventRepository.save(paymentEvent);
            return CheckoutResult.created(paymentEvent);

        } catch (CompletionException e) {
            // 🚨 CASE 1: 비동기 재고 감소 중 실패 (Optimistic Lock 충돌, InterruptedException 등)

            // 🟢 보상 트랜잭션 실행: 재고 복구 시도
            compensateStock(itemsToCompensate, "Async Stock Decrease Failed");

            Throwable actualException = e.getCause();
            String errorMessage = actualException.getMessage() != null ? actualException.getMessage() : "";

            if (errorMessage.contains("Interrupted")) {
                Thread.currentThread().interrupt();
                throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, actualException);
            } else if (errorMessage.contains("현재 서버 부하로 결제 실패")) {
                // Optimistic Lock 재시도 실패 -> CONCURRENCY_CONFLICT 반환
                throw new BusinessLogicException(CheckoutErrorCode.CONCURRENCY_CONFLICT, actualException);
            } else if (errorMessage.contains("재고 감소 작업 중 복구 불가능한 비즈니스 실패")) {
                // 재고 부족 등 -> STOCK_UNDERFLOW 반환
                throw new BusinessLogicException(CheckoutErrorCode.STOCK_UNDERFLOW, actualException);
            }

            // 그 외 예상치 못한 오류
            throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, actualException);

        } catch (DataIntegrityViolationException ex) {
            // 🚨 CASE 2: PaymentEvent 저장 중 실패 (Duplicate order_id 충돌 등)

            // 🟢 보상 트랜잭션 실행: 재고 복구 시도
            compensateStock(itemsToCompensate, "Duplicate Order ID Conflict");

            if (isDuplicateKeyError(ex)) {
                Optional<PaymentCheckoutOptDtoForQueryProjection> exist =
                        checkoutFindExistingOrderService.findExistingOrder(paymentEvent.getOrderId());
                if (exist.isPresent()) {
                    CheckoutResult existingResult = mapDtoToCheckoutResult(exist.get());
                    // 이미 처리된 주문이므로 DataConflictWithDataException 발생
                    throw new DataConflictWithDataException(CheckoutErrorCode.ALREADY_PROCESSED_ORDER.getMessage(), existingResult);
                }
            }
            // DuplicateKeyError가 아니거나, 기존 주문을 찾지 못했을 경우
            throw ex;
        } catch (Exception ex) {
            // 🚨 CASE 3: 기타 예상치 못한 오류 (예: DB 연결 오류 등)

            // 🟢 보상 트랜잭션 실행: 재고 복구 시도
            compensateStock(itemsToCompensate, "Unexpected Checkout Error");

            throw new BusinessLogicException(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR, ex);
        }
    }

// -------------------------------------------------------------------------------------------------

    /**
     * 재고 복구를 위한 보상 트랜잭션 실행 메서드.
     * @param items 복구 대상 품목 리스트
     * @param reason 보상 실행 이유 (로깅용)
     */
    private void compensateStock(List<CheckoutItemForQueryProjection2> items, String reason) {
        List<CompletableFuture<Void>> compensationFutures = new ArrayList<>();

        // 로그 기록 (보상 트랜잭션 시작)
        System.out.println("⚠️ Starting Stock Compensation (Reason: " + reason + ") for " + items.size() + " items.");

        for (CheckoutItemForQueryProjection2 item : items) {
            String isbn = item.getIsbn();
            int quantity = item.getAmount();

            // optimisticLockStockFacade.increaseAsync 호출 (비동기 및 REQUIRES_NEW로 구현되어야 함)
            compensationFutures.add(optimisticLockStockFacade.increaseAsync(isbn, quantity));
        }

        try {
            // 보상 작업이 완료될 때까지 대기
            CompletableFuture.allOf(compensationFutures.toArray(new CompletableFuture[0])).join();
            System.out.println("✅ Stock Compensation Completed Successfully.");
        } catch (Exception e) {
            // 보상 실패는 치명적이므로, 반드시 시스템 관리자에게 알림이 가도록 처리해야 합니다.
            System.err.println("🚨 CRITICAL FAILURE: Stock Compensation Failed. Manual intervention required. Items: " + items.toString() + ". Error: " + e.getMessage());
            // 이 시점에서는 예외를 다시 던지지 않고, 로그를 남기고 시스템 에러 알림을 발생시키는 것이 일반적입니다.
        }
    }
}
