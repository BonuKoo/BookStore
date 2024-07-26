package com.myboard.toy.web.naver.client;

import com.myboard.toy.domain.naver.dto.NaverBookDetailViewResponseDto;
import com.myboard.toy.domain.naver.dto.NaverBookListResponseDto;
import com.myboard.toy.web.naver.client.config.NaverFeignConfiguration;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(url = "https://openapi.naver.com/v1/search",
        name = "bookClient",configuration = NaverFeignConfiguration.class)
public interface NaverApiClient {

    //== V1 - String 반환 == //

    //전체 리스트
    @GetMapping("/book.json")
    ResponseEntity<String> getBookInformationListV1(
            @RequestParam("query") String query,
            @RequestParam(value = "display", required = false) Integer display,
            @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "sort", required = false) String sort
    );


    //상세 정보
    @GetMapping("/book_adv.xml")
    ResponseEntity<String> getBookDetailV1(
            @RequestParam(value = "d_titl", required = false) String title,
            @RequestParam(value = "d_isbn", required = false) String isbn,
            @RequestParam(value = "display", required = false, defaultValue = "10") Integer display,
            @RequestParam(value = "start", required = false, defaultValue = "1") Integer start,
            @RequestParam(value = "sort", required = false, defaultValue = "sim") String sort
    );

    //== V2 DTO 반환 == //
    @GetMapping("/book.json")
    ResponseEntity<NaverBookListResponseDto> getBookInformationListV2(
            @RequestParam("query") String query,
            @RequestParam(value = "display", required = false) Integer display,
            @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "sort", required = false) String sort
    );


//            @RequestParam(value = "d_titl", required = false) String title,
    @GetMapping("/book_adv.xml")
    @Headers("Content-Type: application/xml")
    ResponseEntity<NaverBookDetailViewResponseDto> getBookDetailV2(
            @RequestParam(value = "query", required = false) String isbn,
            @RequestParam(value = "display", required = false, defaultValue = "10") Integer display,
            @RequestParam(value = "start", required = false, defaultValue = "1") Integer start,
            @RequestParam(value = "sort", required = false, defaultValue = "sim") String sort
    );

}

/*

    파라미터           필수 여부    설명
    query   : String    Y        검색어. UTF-8로 인코딩되어야 함
    display : Integer   N        표시할 검색 결과 개수  기본 값 : 10, 최댓값 : 100
    start   : Integer   N        검색 시작 위치 (기본 값 : 1, 최댓값 : 1000)
    sort    : String    N        검색 결과 정렬 방법 - sim  : 정확도 순으로 내림차순 정렬 (default)
                                                  - date : 출간ㄴ일순으로 내림차순 정렬
 */

/*
    전체 리스트 URL 예시
    https://openapi.naver.com/v1/search/book.json?query=aws&display=10&start=1&sort=sim

    상세 요청 URL 예시
    https://openapi.naver.com/v1/search/book_adv.xml?d_titl=aws
 */