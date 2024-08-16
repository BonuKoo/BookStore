package com.myboard.toy.web.naver.controller;

import com.myboard.toy.sales.item.service.ItemService;
import com.myboard.toy.web.naver.service.NaverBookService;
import com.myboard.toy.web.naver.dto.NaverBookDetailRequestDto;
import com.myboard.toy.web.naver.dto.NaverBookDetailViewResponseDto;
import com.myboard.toy.web.naver.dto.NaverBookListRequestDto;
import com.myboard.toy.web.naver.dto.NaverBookListResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.NumberFormat;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NaverBookController {

    private final NaverBookService naverBookService;
    private final ItemService itemService;

    @GetMapping("/search-books")
    public String searchBookListByString(
            @RequestParam(value = "query",defaultValue = "aws") String query,
            @RequestParam(value = "display", defaultValue = "100", required = false) Integer display,
            @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "sort", required = false) String sort,
            Model model
    ) {

        NaverBookListRequestDto requestDto = new NaverBookListRequestDto(
                query,
                display != null ? display.toString() : null,
                start != null ? start.toString() : null,
                sort);

        NaverBookListResponseDto responseDto = naverBookService.getBookListByDTO(requestDto);

        // 포맷팅된 값으로 변환
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        responseDto.getItems().forEach(item -> {
            item.setDiscountFormatted(numberFormat.format(item.getDiscount())); // 역슬래시 추가
        });


        model.addAttribute("bookList", responseDto);
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

}

