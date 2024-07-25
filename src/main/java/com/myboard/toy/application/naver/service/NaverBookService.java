package com.myboard.toy.application.naver.service;

import com.myboard.toy.web.naver.client.NaverApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NaverBookService {

    private final NaverApiClient naverApiClient;

    public String getStringBookList(String query, Integer display, Integer start, String sort) {
        ResponseEntity<String> response = naverApiClient.getBookInformationList(query, display, start, sort);
        return response.getBody();
    }

    public String getStringBookDetail(String title, String isbn) {
        ResponseEntity<String> response = naverApiClient.getBookDetail(title, isbn, 10, 1, "sim");
        return response.getBody();
    }

    public ResponseEntity<String> getRestBookList(String query, Integer display, Integer start, String sort) {
        ResponseEntity<String> response = naverApiClient.getBookInformationList(query, display, start, sort);
        return response;
    }

    public ResponseEntity<String> getRestBookDetail(String title, String isbn) {
        ResponseEntity<String> response = naverApiClient.getBookDetail(title, isbn, 10, 1, "sim");
        return response;
    }


}
