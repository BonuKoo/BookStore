package com.bookService.core.infra.toss.service;

import com.bookService.core.common.exception.PSPConfirmationException;
import com.bookService.core.domain.payment.dto.*;
import com.bookService.core.domain.payment.persistent.PaymentStatusUpdateRepository;
import com.bookService.core.domain.payment.persistent.repository.springdata.SpringDataJpaPaymentOrderRepository;
import com.bookService.core.infra.toss.executor.TossPaymentExecutor;
import com.bookService.core.infra.toss.usecase.PaymentConfirmUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentConfirmService implements PaymentConfirmUseCase {

    private final PaymentStatusUpdateRepository paymentStatusUpdateRepository;
    private final SpringDataJpaPaymentOrderRepository springDataJpaPaymentOrderRepository;
    private final TossPaymentExecutor tossPaymentExecutor;

    @Override
    @Transactional
    public PaymentConfirmationResult confirm(PaymentConfirmCommand command) {

        // 상태를 EXECUTING으로 업데이트
        paymentStatusUpdateRepository.updatePaymentStatusToExecuting(command.getOrderId(), command.getPaymentKey());

        // 결제 유효성 검사
        springDataJpaPaymentOrderRepository.isValid(command.getOrderId(), command.getAmount());

        try {
            // 결제 실행
            PaymentExecutionResult result = tossPaymentExecutor.execute(command);

            PaymentStatusUpdateCommand build = PaymentStatusUpdateCommand.builder()
                    .paymentKey(result.getPaymentKey())
                    .orderId(result.getOrderId())
                    .status(result.paymentStatus())
                    .extraDetails(result.getExtraDetails())
                    .failure(result.getFailure())
                    .build();

            // 상태 업데이트
            paymentStatusUpdateRepository.updatePaymentStatus(build);
            // 결과 반환
            return new PaymentConfirmationResult(result.paymentStatus(), result.getFailure());
                /*
                PaymentConfirmationResult.builder()
                .status(result.paymentStatus())
                .failure(result.getFailure())
                .build();
        */
            //예외 처리는 우선 나중에.
            //.orElseThrow(new PaymentAlreadyProcessedException(,"이미 처리된 결제입니다."))
            //byIdempotencyKey.get();
        } catch (PSPConfirmationException e) {
            PaymentFailure failure = new PaymentFailure(e.getErrorCode(), e.getMessage());

            PaymentStatusUpdateCommand updateCommand = PaymentStatusUpdateCommand.builder()
                    .paymentKey(command.getPaymentKey())
                    .orderId(command.getOrderId()) // orderId 를 어디서 가져올 수 있을지 확인 필요
                    .status(e.paymentStatus()) // FAILURE 또는 UNKNOWN
                    .failure(failure)
                    .build();

            // 실패 상태로 업데이트
            paymentStatusUpdateRepository.updatePaymentStatus(updateCommand);

            // 실패 결과 반환
            return new PaymentConfirmationResult(e.paymentStatus(), failure);
        }
    }
}

