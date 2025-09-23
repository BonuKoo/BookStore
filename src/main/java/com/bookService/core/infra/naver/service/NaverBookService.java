package com.bookService.core.infra.naver.service;

import com.bookService.core.infra.naver.client.NaverApiClient;
import com.bookService.core.infra.naver.dto.NaverBookDetailRequestDto;
import com.bookService.core.infra.naver.dto.NaverBookDetailViewResponseDto;
import com.bookService.core.infra.naver.dto.NaverBookListRequestDto;
import com.bookService.core.infra.naver.dto.NaverBookListResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j @Service
@RequiredArgsConstructor
public class NaverBookService {
    private final NaverApiClient naverApiClient;

    public NaverBookListResponseDto getBookListByDTO(NaverBookListRequestDto requestDto) {
        String sort = requestDto.getSort();

        // sort 값이 null이거나 비어 있으면 기본값으로 "sim"을 사용
        if (sort == null || sort.trim().isEmpty()) {
            sort = "sim";
        }

        ResponseEntity<NaverBookListResponseDto> response = naverApiClient.getBookInformationListV2(
                requestDto.getQuery(),
                Integer.parseInt(requestDto.getDisplay()),
                Integer.parseInt(requestDto.getStart()),
                sort
        );

        return response.getBody();
    }

    public NaverBookDetailViewResponseDto getBookDetailByDTO(NaverBookDetailRequestDto requestDto) {
        log.info("Service에서 넘어온 파라미터=============================ISBN {}", requestDto.getIsbn());

        ResponseEntity<NaverBookDetailViewResponseDto> response =
                naverApiClient.getBookDetailV2(requestDto.getIsbn(), 10, 1, "sim");

        log.info("Service에서 넘어가는 파라미터 들 - 상세 정보임 ::: {}", response.toString());
        return response.getBody();
    }

}
