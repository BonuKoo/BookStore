package com.myboard.toy.web.toss.controller;

import com.myboard.toy.web.toss.TossPaymentsClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

//@Slf4j
//@RequestMapping("/toss")
//@Controller
@RequiredArgsConstructor
public class WidgetController {

    private final TossPaymentsClient tossPaymentsClient;


    @RequestMapping(value = "/confirm", method = RequestMethod.POST)

    public ResponseEntity<JSONObject> confirmPayment(@RequestBody String jsonBody) throws Exception {

        JSONParser parser = new JSONParser();

        String orderId;
        String amount;
        String paymentKey;

        try {

            JSONObject requestData = (JSONObject) parser.parse(jsonBody);

            orderId = (String) requestData.get("orderId");
            amount = (String) requestData.get("amount");
            paymentKey = (String) requestData.get("paymentKey");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("amount", amount);
        obj.put("paymentKey", paymentKey);

        // 시크릿 키 설정
        String widgetSecretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
        Base64.Encoder encoder = Base64.getEncoder();
        String authorizations = "Basic " + new String(encoder.encode((widgetSecretKey + ":").getBytes(StandardCharsets.UTF_8)));

        // OpenFeign을 사용하여 API 호출
        JSONObject response = tossPaymentsClient.confirmPayment(authorizations, "application/json", obj);

        // HTTP 상태 코드를 200으로 가정하고 설정
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(HttpServletRequest request, Model model) throws Exception {
        return "toss/checkout";
    }

    @RequestMapping(value = "/success", method = RequestMethod.GET)
    public String paymentRequest(HttpServletRequest request, Model model) throws Exception {
        return "toss/success";
    }

    @RequestMapping(value = "/fail", method = RequestMethod.GET)
    public String failPayment(HttpServletRequest request, Model model) throws Exception {
        String failCode = request.getParameter("code");
        String failMessage = request.getParameter("message");

        model.addAttribute("code", failCode);
        model.addAttribute("message", failMessage);

        return "toss/fail";
    }
}
