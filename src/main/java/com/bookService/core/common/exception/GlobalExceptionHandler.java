package com.bookService.core.common.exception;

import com.bookService.core.common.dto.ResponseDTO;
import com.bookService.core.common.exception.checkout.BusinessLogicException;
import com.bookService.core.common.exception.checkout.CheckoutErrorCode;
import com.bookService.core.common.exception.checkout.DataConflictWithDataException;
import com.bookService.core.common.exception.checkout.PaymentProcessingException;
import com.bookService.core.domain.checkout.dto.CheckoutResult;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 데이터(CheckoutResult)를 포함하는 409 Conflict 처리 (메인 핸들러)
    @ExceptionHandler(DataConflictWithDataException.class)
    public ResponseEntity<ResponseDTO<CheckoutResult>> handleDataConflictWithDataException(DataConflictWithDataException ex) {
        ResponseDTO<CheckoutResult> response = ResponseDTO.<CheckoutResult>builder()
                .error(null)
                .data(List.of(ex.getData()))
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // 2. 비즈니스 로직 처리 중 발생한 오류 (ErrorCode 기반 409, 422, 500 처리)
    @ExceptionHandler({PaymentProcessingException.class, BusinessLogicException.class})
    public ResponseEntity<ResponseDTO<?>> handleBusinessLogicExceptions(BusinessLogicException ex) {

        CheckoutErrorCode errorCode = ex.getErrorCode();

        // 에러 코드가 없거나 UNHANDLED_EXCEPTION인 경우 500으로 처리
        if (errorCode == null || errorCode == CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR) {
            ResponseDTO<?> response = ResponseDTO.builder()
                    .error(CheckoutErrorCode.UNEXPECTED_BUSINESS_ERROR.getMessage())
                    .build();
            // log.error("Unhandled Business Logic Error (500): ", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500
        }

        // 에러 코드와 메시지를 사용하여 응답 생성
        ResponseDTO<?> response = ResponseDTO.builder()
                .error(errorCode.getMessage())
                .build();

        // 에러 코드에 정의된 HttpStatus를 사용
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    // DataIntegrityViolationException 처리
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseDTO<?>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // 중복 키가 아닌 DB 무결성 오류이므로, 500 처리
        ResponseDTO<?> response = ResponseDTO.builder()
                .error(CheckoutErrorCode.DB_INTEGRITY_VIOLATION.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500
    }

    // 3. 모든 처리되지 않은 예외 (최종 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<?>> handleAllExceptions(Exception ex) {
        // 최종 Fallback 500 처리 (Enum 사용)
        ResponseDTO<?> response = ResponseDTO.builder()
                .error(CheckoutErrorCode.UNHANDLED_EXCEPTION.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500
    }
}