package com.bookService.core.domain.checkout.service;

import com.bookService.core.common.util.IdempotencyCreator;
import com.bookService.core.domain.checkout.dto.CheckoutCommandForDev;
import com.bookService.core.domain.checkout.dto.CheckoutRequest;
import com.bookService.core.test.checkout.service.CheckoutServiceForDev;
//import net.ttddyy.dsproxy.QueryCount;
//import net.ttddyy.dsproxy.QueryCountHolder;
//import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

//@SpringBootTest
public class CheckoutServiceQueryTest {

    @Autowired
    CheckoutServiceForDev checkoutService;
    /*
    @BeforeEach
    void setUp() {
        QueryCountHolder.clear();  // 모든 카운트 리셋
    }*/

    // CartItem IDs 3개를 가정
    private final String ids = "893";
    private final List<Long> cartItemIdsArray = new ArrayList<>(List.of(209420L, 209421L, 209422L));
    /*
    @Test
    void queryCountTest_BeforeFix() {

        CheckoutRequest request = new CheckoutRequest(cartItemIdsArray, ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString());

        Long userIdLongType = Long.parseLong(ids);

        CheckoutCommandForDev checkoutCommandForDev = CheckoutCommandForDev.builder()
                .buyerId(userIdLongType)
                .cartItemIds(cartItemIdsArray)
                // order_id로 사용될 멱등성 키를 생성합니다.
                .idempotencyKey(IdempotencyCreator.create(request, ids))
                .build();
        checkoutService.checkout1(ids,request);

//        QueryCount count = QueryCountHolder.getGrandTotal();

//        System.out.println("select 수 : "+count.getSelect());
//        System.out.println("insert 수 : "+count.getInsert());
    }

    @Test
    void queryCountTest_AfterFix() {

        CheckoutRequest request = new CheckoutRequest(cartItemIdsArray, ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString());

        Long userIdLongType = Long.parseLong(ids);

        CheckoutCommandForDev checkoutCommandForDev = CheckoutCommandForDev.builder()
                .buyerId(userIdLongType)
                .cartItemIds(cartItemIdsArray)
                // order_id로 사용될 멱등성 키를 생성합니다.
                .idempotencyKey(IdempotencyCreator.create(request, ids))
                .build();
        checkoutService.checkout2(ids,request);

//        QueryCount count = QueryCountHolder.getGrandTotal();

//        System.out.println("select 수 : "+count.getSelect());
//        System.out.println("insert 수 : "+count.getInsert());
    }
    @Test
    void queryCountTest_AfterFix2() {

        CheckoutRequest request = new CheckoutRequest(cartItemIdsArray, ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString());

        Long userIdLongType = Long.parseLong(ids);

        CheckoutCommandForDev checkoutCommandForDev = CheckoutCommandForDev.builder()
                .buyerId(userIdLongType)
                .cartItemIds(cartItemIdsArray)
                // order_id로 사용될 멱등성 키를 생성합니다.
                .idempotencyKey(IdempotencyCreator.create(request, ids))
                .build();
        checkoutService.checkout3(ids,request);

//        QueryCount count = QueryCountHolder.getGrandTotal();

//        System.out.println("select 수 : "+count.getSelect());
//        System.out.println("insert 수 : "+count.getInsert());
    }

    @Test
    void queryCountTest_AfterFix3() {

        CheckoutRequest request = new CheckoutRequest(cartItemIdsArray, ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString());

        Long userIdLongType = Long.parseLong(ids);

        CheckoutCommandForDev checkoutCommandForDev = CheckoutCommandForDev.builder()
                .buyerId(userIdLongType)
                .cartItemIds(cartItemIdsArray)
                // order_id로 사용될 멱등성 키를 생성합니다.
                .idempotencyKey(IdempotencyCreator.create(request, ids))
                .build();
        checkoutService.checkout4(ids,request);

//        QueryCount count = QueryCountHolder.getGrandTotal();

//        System.out.println("select 수 : "+count.getSelect());
//        System.out.println("insert 수 : "+count.getInsert());
    }*/
}
