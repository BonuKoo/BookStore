package com.myboard.toy.web.toss.payment.adapter.in.web.response;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ApiResponse <T>{

    private int status;
    private String message;
    private T data;

    public ApiResponse() {
        this.status = 200;
        this.message = "";
        this.data = null;
    }
    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> with(HttpStatus httpStatus, String message, T data) {
        return new ApiResponse<>(httpStatus.value(), message, data);
    }
}
