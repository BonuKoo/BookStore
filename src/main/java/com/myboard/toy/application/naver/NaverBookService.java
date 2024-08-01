package com.myboard.toy.application.naver;

import com.myboard.toy.domain.naver.dto.NaverBookDetailRequestDto;
import com.myboard.toy.domain.naver.dto.NaverBookDetailViewResponseDto;
import com.myboard.toy.domain.naver.dto.NaverBookListRequestDto;
import com.myboard.toy.domain.naver.dto.NaverBookListResponseDto;
import com.myboard.toy.web.naver.client.NaverApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverBookService {

    private final NaverApiClient naverApiClient;

    public String getStringBookList(String query, Integer display, Integer start, String sort) {
        ResponseEntity<String> response = naverApiClient.getBookInformationListV1(query, display, start, sort);
        return response.getBody();
    }

    public String getStringBookDetail(String title, String isbn) {
        ResponseEntity<String> response = naverApiClient.getBookDetailV1(title, isbn, 10, 1, "sim");
        return response.getBody();
    }


    public ResponseEntity<String> getRestBookList(String query, Integer display, Integer start, String sort) {
        ResponseEntity<String> response = naverApiClient.getBookInformationListV1(query, display, start, sort);
        return response;
    }


    public ResponseEntity<String> getRestBookDetail(String title, String isbn) {
        ResponseEntity<String> response = naverApiClient.getBookDetailV1(title, isbn, 10, 1, "sim");
        return response;
    }

                                    //== 정의된 DTO==//
    public NaverBookListResponseDto getBookListByDTO(NaverBookListRequestDto requestDto) {

        ResponseEntity<NaverBookListResponseDto> response = naverApiClient.getBookInformationListV2(
                requestDto.getQuery(),
                requestDto.getDisplay() != null ? Integer.parseInt(requestDto.getDisplay()) : null,
                requestDto.getStart() != null ? Integer.parseInt(requestDto.getStart()) : null,
                requestDto.getSort()
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
