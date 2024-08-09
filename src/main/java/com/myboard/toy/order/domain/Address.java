package com.myboard.toy.order.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
@Builder
public class Address {
    private String postcode;
    private String roadAddress;   // 도로명 주소
    private String jibunAddress;  // 지번 주소
    private String detailAddress; // 상세 주소
    private String extraAddress;  // 참고 항목
}