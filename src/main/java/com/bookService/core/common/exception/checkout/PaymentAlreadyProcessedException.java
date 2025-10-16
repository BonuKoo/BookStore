package com.bookService.core.common.exception.checkout;


import com.bookService.core.domain.payment.enumtype.PaymentStatus;

public class PaymentAlreadyProcessedException extends RuntimeException {

  private final PaymentStatus status;

  public PaymentAlreadyProcessedException(String message, PaymentStatus status) {
    super(message);
    this.status = status;
  }
  public PaymentStatus getStatus(){
    return status;
  }
}
