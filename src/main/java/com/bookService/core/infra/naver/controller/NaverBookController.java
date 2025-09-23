package com.bookService.core.infra.naver.controller;

import com.bookService.core.domain.item.service.ItemService;
import com.bookService.core.infra.naver.dto.NaverBookDetailRequestDto;
import com.bookService.core.infra.naver.dto.NaverBookDetailViewResponseDto;
import com.bookService.core.infra.naver.dto.NaverBookListRequestDto;
import com.bookService.core.infra.naver.dto.NaverBookListResponseDto;
import com.bookService.core.infra.naver.service.NaverBookService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/naver")
public class NaverBookController {

    private final NaverBookService naverBookService;
    private final ItemService itemService;

    @GetMapping("/search-books")
    public  List<NaverBook> searchBookListByString(
            @RequestParam(value = "query", defaultValue = "aws") String query,
            @RequestParam(value = "display", defaultValue = "10") Integer display,
            @RequestParam(value = "start", defaultValue = "1") Integer start
            //,@AuthenticationPrincipal String userId
    ) {
        //log.info("User {} is searching books with query '{}'", userId, query);

        // requestDto 생성
        NaverBookListRequestDto requestDto = NaverBookListRequestDto.builder()
                .query(query)
                .display(display.toString())
                .start(start.toString())
                .build();

        // API 호출 및 결과 처리
        NaverBookListResponseDto responseDto = naverBookService.getBookListByDTO(requestDto);

        // 데이터 변환 및 포맷팅
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

        List<NaverBook> books = responseDto.getItems().stream()
                .map(item -> new NaverBook(
                        item.getTitle(),
                        item.getIsbn(),
                        item.getImage(),
                        item.getAuthor(),
                        numberFormat.format(item.getDiscount())
                ))
                .toList();

        return books;
    }

    //네이버에서 값을 그대로 가져오는 중
    @GetMapping("/bookDetail")
    public NaverBookDetailViewResponseDto searchBookDetailByStringV1(@RequestParam String isbn
                                                                     //,@AuthenticationPrincipal String userId
    ) {

        //log.info("User {} is requesting book detail for ISBN {}", userId, isbn);

        // ISBN을 사용하여 상세 정보 가져오기
        NaverBookDetailRequestDto requestDto = new NaverBookDetailRequestDto(isbn);

        NaverBookDetailViewResponseDto bookDetail = naverBookService.getBookDetailByDTO(requestDto);
        //받아온 데이터 저장
        setUpItemData(bookDetail);

        return bookDetail;
    }

    void setUpItemData(NaverBookDetailViewResponseDto data){
        itemService.saveItem(data);
    }

    @Getter
    @AllArgsConstructor
    private static class NaverBook{
        private String title;
        private String isbn;
        private String image;
        private String author;
        private String discountFormatted;
    }
}
