package com.myboard.toy.controller.naver.controller;

import com.myboard.toy.application.naver.service.NaverBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NaverBookController {

    private final NaverBookService naverBookService;

    //== String 타입 반환 ==//

    @GetMapping("/search-books")
    public String searchBookListByString(
            @RequestParam("query") String query,
            @RequestParam(value = "display", required = false) Integer display,
            @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        return naverBookService.getStringBookList(query, display, start, sort);
    }

    @GetMapping("/book/detail")
    public String searchBookDetailByString(@RequestParam String title, @RequestParam String isbn) {
        return naverBookService.getStringBookDetail(title, isbn);
    }

    //== Rest Body 반환 ==//
    @GetMapping("/api/search-books")
    public ResponseEntity<String> searchBookListByRest(
            @RequestParam("query") String query,
            @RequestParam(value = "display", required = false) Integer display,
            @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        return naverBookService.getRestBookList(query, display, start, sort);
    }

    @GetMapping("/api/book/detail")
    public ResponseEntity<String> searchBookDetailByRest(
            @RequestParam String title,
            @RequestParam String isbn
    ) {
        return naverBookService.getRestBookDetail(title, isbn);
    }
}
