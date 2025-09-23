package com.bookService.core.common.exception;

import com.bookService.core.domain.payment.enumtype.PaymentStatus;

public class PSPConfirmationException extends RuntimeException {
  private final String errorCode;
  private final String errorMessage;
  private final boolean isSuccess;
  private final boolean isFailure;
  private final boolean isUnknown;
  private final boolean isRetryableError;

  public PSPConfirmationException(
          String errorCode,
          String errorMessage,
          boolean isSuccess,
          boolean isFailure,
          boolean isUnknown,
          boolean isRetryableError,
          Throwable cause
  ) {
    super(errorMessage, cause);
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.isSuccess = isSuccess;
    this.isFailure = isFailure;
    this.isUnknown = isUnknown;
    this.isRetryableError = isRetryableError;

    if (!isSuccess && !isFailure && !isUnknown) {
      throw new IllegalArgumentException(this.getClass().getSimpleName() + " 는 올바르지 않은 결제 상태를 가지고 있습니다.");
    }
  }

  public PSPConfirmationException(
          String errorCode,
          String errorMessage,
          boolean isSuccess,
          boolean isFailure,
          boolean isUnknown,
          boolean isRetryableError
  ) {
    this(errorCode, errorMessage, isSuccess, isFailure, isUnknown, isRetryableError, null);
  }

  public String getErrorCode() {
    return errorCode;
  }

  @Override
  public String getMessage() {
    return errorMessage;
  }

  public boolean isSuccess() {
    return isSuccess;
  }

  public boolean isFailure() {
    return isFailure;
  }

  public boolean isUnknown() {
    return isUnknown;
  }

  public boolean isRetryableError() {
    return isRetryableError;
  }

  public PaymentStatus paymentStatus() {
    if (isSuccess) {
      return PaymentStatus.SUCCESS;
    } else if (isFailure) {
      return PaymentStatus.FAILURE;
    } else if (isUnknown) {
      return PaymentStatus.UNKNOWN;
    } else {
      throw new IllegalStateException(this.getClass().getSimpleName() + " 는 올바르지 않은 결제 상태를 가지고 있습니다.");
    }
  }
}
