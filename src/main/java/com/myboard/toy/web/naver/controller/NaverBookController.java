package com.myboard.toy.web.naver.controller;

import com.myboard.toy.sales.item.service.ItemService;
import com.myboard.toy.web.naver.service.NaverBookService;
import com.myboard.toy.web.naver.dto.NaverBookDetailRequestDto;
import com.myboard.toy.web.naver.dto.NaverBookDetailViewResponseDto;
import com.myboard.toy.web.naver.dto.NaverBookListRequestDto;
import com.myboard.toy.web.naver.dto.NaverBookListResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NaverBookController {

    private final NaverBookService naverBookService;
    private final ItemService itemService;


    @GetMapping("/search-books")
    public String searchBookListByString(
            @RequestParam(value = "query", defaultValue = "aws") String query,
            @RequestParam(value = "page", defaultValue = "1") Integer page, // 현재 페이지
            @RequestParam(value = "sort", required = false) String sort, // 정렬
            Model model
    ) {

// 페이지에 따라 start 값을 계산합니다.
        Integer start = (page - 1) / 10 + 1;
        final Integer DISPLAY = 100;
        // API 요청 시 항목 수 (이것은 고정)
        // requestDto 생성
        NaverBookListRequestDto requestDto = NaverBookListRequestDto.builder()
                .query(query)
                .display("100")
                .start(start.toString())
                .build();

        // API 호출 및 결과 처리
        NaverBookListResponseDto responseDto = naverBookService.getBookListByDTO(requestDto);

        // 데이터 변환 및 포맷팅
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

        //응답 값을 books로 매핑
        List<NaverBook> books = responseDto.getItems().stream()
                .map(item -> new NaverBook(
                        item.getTitle(),
                        item.getIsbn(),
                        item.getImage(),
                        item.getAuthor(),
                        numberFormat.format(item.getDiscount())
                ))
                .toList();

        int pageSize = 10;
        int fromIndex = (page - 1) % 10 * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, books.size());

        List<NaverBook> pagedList = books.subList(fromIndex, toIndex);

        model.addAttribute("bookList", pagedList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) books.size() / pageSize));

        return "/book/listbynaver";
    }

    //네이버에서 값을 그대로 가져오는 중
    @GetMapping("/bookDetail")
    public String searchBookDetailByStringV1(@RequestParam String isbn, Model model) {

        // ISBN을 사용하여 상세 정보 가져오기
        NaverBookDetailRequestDto requestDto = new NaverBookDetailRequestDto(isbn);

        NaverBookDetailViewResponseDto bookDetail = naverBookService.getBookDetailByDTO(requestDto);
        //받아온 데이터 저장
        setUpItemData(bookDetail);
        model.addAttribute("bookDetail", bookDetail);
        return "/book/detailbynaver";
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

