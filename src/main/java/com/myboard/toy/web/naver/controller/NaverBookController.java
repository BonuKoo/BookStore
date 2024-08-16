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
            @RequestParam(value = "display", defaultValue = "5") Integer display,
            @RequestParam(value = "start", defaultValue = "1") Integer start,
            Model model
    ) {

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

// 모델에 필요한 데이터 추가
        model.addAttribute("bookList", books);
        model.addAttribute("query", query);
        model.addAttribute("display", display);
        model.addAttribute("start", start);
        model.addAttribute("hasNext", responseDto.getItems().size() == display); // 다음 페이지가 있는지 확인

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

