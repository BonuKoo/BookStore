package com.bookService.core.domain.payment.usecase;

import com.bookService.core.domain.checkout.dto.CheckoutRequest;
import com.bookService.core.domain.checkout.dto.CheckoutResult;

public interface CheckoutUseCase {

    CheckoutResult checkout(String userId, CheckoutRequest request);
}

/*
checkout1 :  //== userId로 Cart를 조회 후 command에 담는다 ==//
checkout2:   //== 데이터베이스와의 통신을 줄이기 위해, cartId를 제외하고 Command에는 userId만 담는다. 쿼리 발생 횟수를 감소 ==//
checkout3:   //== SocketI/O 지연 및 JVM 메모리 부하를 줄이기 위해, Fetch가 아닌 QueryProjection을 활용해서 필요한 데이터만 선별  ==//
checkout4:   // == 중복 검사인 경우를 DTO 프로젝션으로 로딩 최적화 == //
checkout6_1: //== Unique Key를 활용한 try-catch ==//
checkout6_2:
checkout6_3: //== 트랜잭션 매니저를 열어서 확인 ==//
checkout6_4:
checkout6_5:

checkout7 : //== Custom Exception을 만들고, Service 에서 오류를 감지 후
                 오류 상황에 따라 분류
                 저장 성공    : SUCCESS         200
                 중복 저장    : ALREADY_EXISTS  409
                 잘못된 상황   : FAILED          500  ==//
 */