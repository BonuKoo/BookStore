package com.bookService.core.common.exception;

import com.bookService.core.common.dto.ResponseDTO;
import com.bookService.core.common.exception.checkout.BusinessLogicException;
import com.bookService.core.common.exception.checkout.CheckoutAlreadyExistsException;
import com.bookService.core.common.exception.checkout.DataConflictWithDataException;
import com.bookService.core.common.exception.checkout.PaymentProcessingException;
import com.bookService.core.domain.checkout.dto.CheckoutResult;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 데이터(CheckoutResult)를 포함하는 409 Conflict 처리 (메인 핸들러)
    @ExceptionHandler(DataConflictWithDataException.class)
    public ResponseEntity<ResponseDTO<CheckoutResult>> handleDataConflictWithDataException(DataConflictWithDataException ex) {
        // Custom Exception에서 데이터를 꺼내 data 필드에 넣습니다.
        ResponseDTO<CheckoutResult> response = ResponseDTO.<CheckoutResult>builder()
                .error(null) // 성공적인 데이터 반환이므로 error는 null
                .data(List.of(ex.getData())) // 데이터를 리스트에 담아 data 필드에 채움
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409
    }

    // DataIntegrityViolationException을 409로 처리하고 싶다면, Service에서 바로
    // DataConflictWithDataException으로 변환하여 던지거나,
    // 아래와 같이 DataIntegrityViolationException을 500으로 처리하는 일반 핸들러를 유지합니다.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseDTO<?>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // 중복 키는 Service에서 이미 DataConflictWithDataException으로 변환했으므로,
        // 여기서 잡히는 것은 다른 형태의 DB 무결성 오류입니다.
        // log.error("DB 무결성 위반: ", ex);
        ResponseDTO<?> response = ResponseDTO.builder()
                .error("데이터베이스 무결성 제약 조건 위반 오류가 발생했습니다.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500
    }

    // 2. 비즈니스 로직 처리 중 발생한 예상치 못한 오류 (500)
    @ExceptionHandler({PaymentProcessingException.class, BusinessLogicException.class})
    public ResponseEntity<ResponseDTO<?>> handleBusinessLogicException(RuntimeException ex) {
        ResponseDTO<?> response = ResponseDTO.builder()
                .error("결제 처리 중 오류가 발생했습니다.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500
    }

    // 3. 모든 처리되지 않은 예외 (최종 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<?>> handleAllExceptions(Exception ex) {
        // log.error("Unhandled Exception: ", ex);
        ResponseDTO<?> response = ResponseDTO.builder()
                .error("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500
    }

}
