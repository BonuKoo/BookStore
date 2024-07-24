package com.myboard.toy.api.item.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(url = "https://openapi.naver.com/v1/search/book",name = "bookListClient")
public interface ApiClient {



}
