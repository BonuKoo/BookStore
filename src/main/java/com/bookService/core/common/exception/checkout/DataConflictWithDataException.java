package com.bookService.core.common.exception.checkout;

import com.bookService.core.domain.checkout.dto.CheckoutResult;

public class DataConflictWithDataException extends RuntimeException {
    private final CheckoutResult data;

    public DataConflictWithDataException(String message, CheckoutResult data) {
      super(message);
      this.data = data;
    }

    public CheckoutResult getData() {
      return data;
    }
}
