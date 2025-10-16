package com.bookService.core.test.checkout.service;

import com.bookService.core.common.exception.checkout.BusinessLogicException;
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
import com.bookService.core.test.cart.exception.CartItemNotExistException;
import com.bookService.core.test.checkout.repository.CheckoutTransactionRepository;
import com.bookService.core.domain.payment.dto.PaymentCheckoutOptDtoForQueryProjection;
import com.bookService.core.domain.payment.enumtype.PaymentStatus;
import com.bookService.core.domain.payment.entity.PaymentEvent;
import com.bookService.core.domain.payment.entity.PaymentOrder;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentEventRepository;
import com.bookService.core.domain.payment.usecase.CheckoutFindExistingOrderUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;
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
            // 낙관적 락킹 실패, 기타 예상치 못한 오류 등을 Custom Exception으로 감싸 던진다..
            // 낙관적 락킹 실패는 throw new DataConflictException("동시성 충돌"); 와 같이 처리
            throw new BusinessLogicException("결제 처리 중 서버 오류", ex);
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
            // 낙관적 락킹 실패, 기타 예상치 못한 오류 등을 Custom Exception으로 감싸 던진다..
            // 낙관적 락킹 실패는 throw new DataConflictException("동시성 충돌"); 와 같이 처리
            throw new BusinessLogicException("결제 처리 중 서버 오류", ex);
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
            // 낙관적 락킹 실패, 기타 예상치 못한 오류 등을 Custom Exception으로 감싸 던진다..
            // 낙관적 락킹 실패는 throw new DataConflictException("동시성 충돌"); 와 같이 처리
            throw new BusinessLogicException("결제 처리 중 서버 오류", ex);
        }
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
            // 낙관적 락킹 실패, 기타 예상치 못한 오류 등을 Custom Exception으로 감싸 던진다..
            // 낙관적 락킹 실패는 throw new DataConflictException("동시성 충돌"); 와 같이 처리
            throw new BusinessLogicException("결제 처리 중 서버 오류", ex);
        }
    }

    public CheckoutResult checkout9_5(String userId, CheckoutRequest request){

        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);
        List<CheckoutItemForQueryProjection2> cartItems =
                checkoutReadService.getCartItemsForEventCreation(checkoutCommandForDev.getCartItemIds());
        
        // 재고 확인


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
            // 낙관적 락킹 실패, 기타 예상치 못한 오류 등을 Custom Exception으로 감싸 던진다..
            // 낙관적 락킹 실패는 throw new DataConflictException("동시성 충돌"); 와 같이 처리
            throw new BusinessLogicException("결제 처리 중 서버 오류", ex);
        }
    }
    // Lock
    public CheckoutResult checkout10_1(String userId, CheckoutRequest request){

        CheckoutCommandForDev checkoutCommandForDev = getCheckoutCommand(userId, request);

        List<CheckoutItemForQueryProjection2> cartItems =
                checkoutReadService.getCartItemsForEventCreation(checkoutCommandForDev.getCartItemIds());

        // 재고 확인
        for (CheckoutItemForQueryProjection2 cartItem: cartItems){
            String isbn = cartItem.getIsbn();
            int quantity = cartItem.getAmount();

            if (isbn==null || isbn.isEmpty()){
                throw new CartItemNotExistException("장바구니 항목 ID[" + cartItem.getCartItemId() + "]에 연결된 Item ISBN이 누락되었습니다.");
            }
            try {
                optimisticLockStockFacade.decrease(isbn,quantity);
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
                throw new CartItemNotExistException("재고 감소 중 오류 발생" + e);
            } catch (RuntimeException e){
                // 재고 부족 등의 RuntimeException 감지
                throw new CartItemNotExistException("Item Quantity Error : " + e.getMessage());
            }
        }

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
            // 낙관적 락킹 실패, 기타 예상치 못한 오류 등을 Custom Exception으로 감싸 던진다..
            // 낙관적 락킹 실패는 throw new DataConflictException("동시성 충돌"); 와 같이 처리
            throw new BusinessLogicException("결제 처리 중 서버 오류", ex);
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
//        List<CheckoutItemForQueryProjection2> cartItems = cartItemRepository.customCartItemProjection2(cartItemIds);

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

}
