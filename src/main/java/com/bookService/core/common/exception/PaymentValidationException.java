package com.bookService.core.common.exception;

public class PaymentValidationException extends RuntimeException {
  public PaymentValidationException(String message) {
    super(message);
  }
}
