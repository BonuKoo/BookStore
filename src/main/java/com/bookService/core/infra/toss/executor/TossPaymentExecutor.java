package com.bookService.core.infra.toss.executor;

import com.bookService.core.common.exception.PSPConfirmationException;
import com.bookService.core.common.exception.TossPaymentError;
import com.bookService.core.domain.payment.enumtype.PSPConfirmationStatus;
import com.bookService.core.domain.payment.enumtype.PaymentMethod;
import com.bookService.core.domain.payment.enumtype.PaymentType;
import com.bookService.core.domain.payment.dto.PaymentConfirmCommand;
import com.bookService.core.domain.payment.dto.PaymentExecutionResult;
import com.bookService.core.domain.payment.dto.PaymentExtraDetails;
import com.bookService.core.domain.payment.dto.TossFailureResponse;
import com.bookService.core.domain.payment.port.PaymentExecutorPort;
import com.bookService.core.infra.toss.TossPaymentClient;
import com.bookService.core.infra.toss.dto.TossPaymentConfirmRequest;
import com.bookService.core.infra.toss.dto.TossPaymentConfirmationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class TossPaymentExecutor implements PaymentExecutorPort {

    private final TossPaymentClient tossPaymentClient;
    private final String authorizationHeader;

    public TossPaymentExecutor(TossPaymentClient tossPaymentClient, String authorizationHeader) {
        this.tossPaymentClient = tossPaymentClient;
        this.authorizationHeader = authorizationHeader;
    }

    /**
     재시도
     maxAttempts = 3 : 최대 3번 시도
     delay = 1000 : 1초 간격
     multiplier = 2.0: 지수 백오프
     */
    /*
    @Retryable(
            value = { PSPConfirmationException.class, TimeoutException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0, random = true)
    )
    */
    public PaymentExecutionResult execute(PaymentConfirmCommand command) {

        log.info("[Toss PaymentConfirmCommand] paymentKey={}, idempotencyKey={}, response={}", command.getPaymentKey(), command.getOrderId(), command.getAmount());

        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(
                command.getPaymentKey(),
                command.getOrderId(),
                command.getAmount()
        );

        try {
            // 요청 시도
            TossPaymentConfirmationResponse response = tossPaymentClient.confirmPayment(command.getOrderId(),
                    authorizationHeader,
                    request);
            //결제 승인 결과 객체 생성 및 반환

            log.info("[Toss결제성공] paymentKey={}, idempotencyKey={}, response={}",
                    command.getPaymentKey(), command.getOrderId(), response);


            PaymentExecutionResult paymentExecutionResult = new PaymentExecutionResult(
                    command.getPaymentKey(),
                    command.getOrderId(),
                    new PaymentExtraDetails(
                            PaymentType.get(response.getType()),
                            PaymentMethod.get(response.getMethod()),
                            LocalDateTime.parse(response.getApprovedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                            response.getOrderName(),
                            PSPConfirmationStatus.get(response.getStatus()),
                            response.getTotalAmount(),
                            response.toString()
                    ),
                    null,
                    true, false, false, false
            );
            return paymentExecutionResult;



            // 결제 실패 시 응답 처리
        } catch (FeignException e){

            log.warn("[Toss결제실패] FeignException 발생: status={}, content={}", e.status(), e.contentUTF8());

            TossFailureResponse failure = extractFailureResponse(e);
            TossPaymentError error = TossPaymentError.get(failure.getCode());

            throw new PSPConfirmationException(
                    error.name(),
                    error.getDescription(),
                    error.isSuccess(),
                    error.isFailure(),
                    error.isUnknown(),
                    error.isRetryableError()
            );
        }

    }

    private TossFailureResponse extractFailureResponse(FeignException e) {
        try {

            String json = e.contentUTF8(); // OpenFeign 11+ 에서 가능
            log.warn("[Toss결제실패] 응답 본문 JSON: {}", json);

            ObjectMapper mapper = new ObjectMapper();

            TossFailureResponse tossFailureResponse = mapper.readValue(json, TossFailureResponse.class);
            log.warn("[Toss결제실패] 파싱된 TossFailureResponse: {}", tossFailureResponse);
            return tossFailureResponse;

        } catch (Exception ex) {
            log.error("Toss 오류 응답 파싱 실패", ex);
            throw new RuntimeException("Toss 오류 응답 파싱 실패", ex);
        }
    }

}
