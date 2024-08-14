package com.myboard.toy.payment.adapter.in.web.api;

import com.myboard.toy.common.WebAdapter;
import com.myboard.toy.payment.adapter.in.web.request.TossPaymentConfirmRequest;
import com.myboard.toy.payment.adapter.in.web.response.ApiResponse;
import com.myboard.toy.payment.adapter.out.web.executor.TossPaymentExecutor;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@WebAdapter
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/toss")
public class TossPaymentController {

    private final TossPaymentExecutor tossPaymentExecutor;

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<JSONObject>> confirm(@RequestBody TossPaymentConfirmRequest request) {

        // TossPaymentExecutor를 통해 동기적으로 API 호출 및 결과 반환

        JSONObject response = tossPaymentExecutor.execute(
                request.getPaymentKey(),
                request.getOrderId(),
                request.getAmount().toString()
        );

        return ResponseEntity.ok().body(
                ApiResponse.with(HttpStatus.OK, "OK", response)
        );
    }
}
