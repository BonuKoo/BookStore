package com.myboard.toy.order.domain;

import com.myboard.toy.security.domain.dto.AccountDto;
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

    public void update(AccountDto dto) {
        if (dto.getPostcode() != null) {
            this.postcode = dto.getPostcode();
        }
        if (dto.getRoadAddress() != null) {
            this.roadAddress = dto.getRoadAddress();
        }
        if (dto.getJibunAddress() != null) {
            this.jibunAddress = dto.getJibunAddress();
        }
        if (dto.getDetailAddress() != null) {
            this.detailAddress = dto.getDetailAddress();
        }
        if (dto.getExtraAddress() != null) {
            this.extraAddress = dto.getExtraAddress();
        }
    }
}