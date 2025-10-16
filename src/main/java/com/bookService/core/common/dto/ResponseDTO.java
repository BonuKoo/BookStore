package com.bookService.core.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
// 공통 API 응답 포맷을 제공하는 제네릭 DTO Class
public class ResponseDTO<T> {

    private String error; // 에러 메시지
    private List<T> data; // 실제 응답 데이터 리스트

}
