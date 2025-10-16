package com.bookService.core.test.checkout.controller;

import com.bookService.core.common.dto.ResponseDTO;
import com.bookService.core.common.exception.checkout.CheckoutException;
import com.bookService.core.common.util.IdempotencyCreator;
import com.bookService.core.domain.checkout.dto.CheckoutCommandForDev;
import com.bookService.core.domain.checkout.dto.CheckoutRequest;
import com.bookService.core.domain.checkout.dto.CheckoutResult;
import com.bookService.core.domain.checkout.enumType.CheckoutStatus;
import com.bookService.core.test.checkout.service.CheckoutFindExistingOrderServiceForDev;
import com.bookService.core.test.checkout.service.CheckoutServiceForDev;
import com.bookService.core.domain.payment.dto.PaymentCheckoutOptDtoForQueryProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/checkout/test")
@RequiredArgsConstructor
public class RestCheckoutControllerForDev {

    private final CheckoutServiceForDev checkoutService;
    private final CheckoutFindExistingOrderServiceForDev existingOrderService;

    @PostMapping("/1")
    public ResponseEntity<CheckoutResult> checkout1(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {

        Long userIdLongType = Long.parseLong(userId);
        CheckoutCommandForDev checkoutCommandForDev = CheckoutCommandForDev.builder()
                .buyerId(userIdLongType)
                .cartItemIds(request.getCartItemIds())
                // order_id로 사용될 멱등성 키를 생성합니다.
                .idempotencyKey(IdempotencyCreator.create(request, userId))
                .build();


        CheckoutResult result = checkoutService.checkout1(userId, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/2")
    public ResponseEntity<CheckoutResult> checkout2(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        CheckoutResult result = checkoutService.checkout2(userId, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/3")
    public ResponseEntity<CheckoutResult> checkout3(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        CheckoutResult result = checkoutService.checkout3(userId, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/4")
    public ResponseEntity<CheckoutResult> checkout4(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        CheckoutResult result = checkoutService.checkout4(userId, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/5")
    public ResponseEntity<CheckoutResult> checkout5(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        CheckoutResult result = checkoutService.checkout5(userId, request);
        return ResponseEntity.ok(result);
    }


    // 트랜잭션 분리가 되질 않는다..
    @PostMapping("/6_2")
    public ResponseEntity<CheckoutResult> checkout6_2(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        Long userIdLongType = Long.parseLong(userId);
        CheckoutCommandForDev checkoutCommandForDev = CheckoutCommandForDev.builder()
                .buyerId(userIdLongType)
                .cartItemIds(request.getCartItemIds())
                // order_id로 사용될 멱등성 키를 생성합니다.
                .idempotencyKey(IdempotencyCreator.create(request, userId))
                .build();

        try {
            CheckoutResult result = checkoutService.checkout6_2(checkoutCommandForDev);
            return ResponseEntity.ok(result);
        } catch (DataIntegrityViolationException e) {
            Optional<PaymentCheckoutOptDtoForQueryProjection> existingOrder =
                    existingOrderService.findExistingOrder(checkoutCommandForDev.getIdempotencyKey());

            if (existingOrder.isPresent()) {
                PaymentCheckoutOptDtoForQueryProjection paymentEvent = existingOrder.get();
                CheckoutResult result = new CheckoutResult(
                        paymentEvent.getTotalAmount(),
                        paymentEvent.getOrderId(),
                        paymentEvent.getOrderName());
                return ResponseEntity.ok(result);
            }
            throw e;
        }
    }

    // 트랜잭션 분리가 되질 않는다..
    @PostMapping("/6_4")
    public ResponseEntity<CheckoutResult> checkout6_4(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        Long userIdLongType = Long.parseLong(userId);
        CheckoutCommandForDev checkoutCommandForDev = CheckoutCommandForDev.builder()
                .buyerId(userIdLongType)
                .cartItemIds(request.getCartItemIds())
                // order_id로 사용될 멱등성 키를 생성합니다.
                .idempotencyKey(IdempotencyCreator.create(request, userId))
                .build();

        CheckoutResult result = checkoutService.checkout6_4(userId, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/6_5")
    public ResponseEntity<CheckoutResult> checkout6_5(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        Long userIdLongType = Long.parseLong(userId);
        CheckoutCommandForDev checkoutCommandForDev = CheckoutCommandForDev.builder()
                .buyerId(userIdLongType)
                .cartItemIds(request.getCartItemIds())
                // order_id로 사용될 멱등성 키를 생성합니다.
                .idempotencyKey(IdempotencyCreator.create(request, userId))
                .build();

        try {
            CheckoutResult result = checkoutService.checkout6_5(checkoutCommandForDev);
            return ResponseEntity.ok(result);
        } catch (DataIntegrityViolationException e) {

            Optional<PaymentCheckoutOptDtoForQueryProjection> existingOrder = Optional.empty();

            int maxRetries = 3;
            long retryDelayMillis = 2000; // 100ms 대기
            for (int i = 0; i < maxRetries; i++) {
                log.debug(" 기존 Payment 반환 시도 : {} - Retry{}/{}", checkoutCommandForDev.getIdempotencyKey(), i + 1, maxRetries);
                existingOrder = existingOrderService.findExistingOrder(checkoutCommandForDev.getIdempotencyKey());

                if (existingOrder.isPresent()) {
                    log.info("======================반복 횟수================={}, orderID: {}", i + 1, checkoutCommandForDev.getIdempotencyKey());
                    break;
                }
                if (i < maxRetries - 1) {
                    try {
                        log.debug("찾지 못한 횟수 {}. 대기 시간 {}ms 전 시도", i + 1, retryDelayMillis);
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

            CheckoutResult result = CheckoutResult.builder()
                    .amount(existingOrderProjection.getTotalAmount())
                    .orderId(existingOrderProjection.getOrderId())
                    .orderName(existingOrderProjection.getOrderName())
                    .build();
            return ResponseEntity.ok(result);


        } catch (Exception e) {
            // 다른 종류의 예외가 발생하면, 트랜잭션을 롤백하고 재처리할 수 있도록 예외를 던집니다.
            log.error("An unexpected error occurred during checkout.", e);
            throw new CheckoutException("An unexpected error occurred.", e);
        }
    }

    // 전략 변경 -> 실패 시 '이미 저장되어있다'는 사실을 활용해서, 중복이면 기존 데이터 조회 후 동일 응답 반환
    // 중복이 아닌 경우 오류 처리

    // == 완료 == //
    @PostMapping("7")
    public ResponseEntity<?> checkout7(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        try {
        CheckoutResult result = checkoutService.checkout7(userId, request);

        ResponseDTO<CheckoutResult> responseDTO =
                ResponseDTO.<CheckoutResult>builder()
                        .data(List.of(result))
                        .build();
        if (result.getStatus()== CheckoutStatus.ALREADY_EXISTS){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseDTO);
        }
        return ResponseEntity.ok(responseDTO);

        }catch (Exception e){
            ResponseDTO<CheckoutResult> responseDTO =
                    ResponseDTO.<CheckoutResult>builder()
                            .error(e.getMessage())
                            .build();
            return ResponseEntity.badRequest().body(responseDTO);
        }
    }

    // == 완료 == //
    @PostMapping("8")
    public ResponseEntity<?> checkout8(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        try {
            CheckoutResult result = checkoutService.checkout8(userId, request);

            ResponseDTO<CheckoutResult> responseDTO =
                    ResponseDTO.<CheckoutResult>builder()
                            .data(List.of(result))
                            .build();
            if (result.getStatus()== CheckoutStatus.ALREADY_EXISTS){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(responseDTO);
            }
            if (result.getStatus() == CheckoutStatus.FAILED){
                responseDTO.setError("결제 처리 중 서버 내부 오류가 발생했습니다.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDTO);
            }
            return ResponseEntity.ok(responseDTO);

        }catch (Exception e){
            ResponseDTO<CheckoutResult> responseDTO =
                    ResponseDTO.<CheckoutResult>builder()
                            .error(e.getMessage())
                            .build();
            return ResponseEntity.badRequest().body(responseDTO);
        }
    }

    /**     테스트 10~14
            테스트 10 : 9_1 : createPaymentEventBeforeRefactoring
            테스트 11 : 9_2 : createPaymentEvent
         2와 3의 차이는 사실 상 AccountEntity를 넣느냐 마느냐 정도의 문제니까 그냥 9_4만 해도 되는듯
            테스트 13 : 9_4 : createPaymentEvent3
     **/

    // 글로벌 익셉션 적용 시작
    @PostMapping("10")
    public ResponseEntity<?> checkout10(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        // 예외 처리는 모두 GlobalExceptionHandler에게 위임
        CheckoutResult result = checkoutService.checkout9_1(userId, request);

        // 성공 응답 (200 OK)
        ResponseDTO<CheckoutResult> responseDTO =
                ResponseDTO.<CheckoutResult>builder()
                        .data(List.of(result))
                        .build();

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("11")
    public ResponseEntity<?> checkout11(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        // 예외 처리는 모두 GlobalExceptionHandler에게 위임
        CheckoutResult result = checkoutService.checkout9_2(userId, request);

        // 성공 응답 (200 OK)
        ResponseDTO<CheckoutResult> responseDTO =
                ResponseDTO.<CheckoutResult>builder()
                        .data(List.of(result))
                        .build();

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("12")
    public ResponseEntity<?> checkout12(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        // 예외 처리는 모두 GlobalExceptionHandler에게 위임
        CheckoutResult result = checkoutService.checkout9_4(userId, request);

        // 성공 응답 (200 OK)
        ResponseDTO<CheckoutResult> responseDTO =
                ResponseDTO.<CheckoutResult>builder()
                        .data(List.of(result))
                        .build();

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("13")
    public ResponseEntity<?> checkout13(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        // 예외 처리는 모두 GlobalExceptionHandler에게 위임
        CheckoutResult result = checkoutService.checkout9_5(userId, request);

        // 성공 응답 (200 OK)
        ResponseDTO<CheckoutResult> responseDTO =
                ResponseDTO.<CheckoutResult>builder()
                        .data(List.of(result))
                        .build();

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("14")
    public ResponseEntity<?> checkout14(
            @AuthenticationPrincipal String userId,
            @RequestBody CheckoutRequest request
    ) {
        // 예외 처리는 모두 GlobalExceptionHandler에게 위임
        CheckoutResult result = checkoutService.checkout9_5(userId, request);

        // 성공 응답 (200 OK)
        ResponseDTO<CheckoutResult> responseDTO =
                ResponseDTO.<CheckoutResult>builder()
                        .data(List.of(result))
                        .build();

        return ResponseEntity.ok(responseDTO);
    }

}