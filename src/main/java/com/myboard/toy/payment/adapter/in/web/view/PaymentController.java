package com.myboard.toy.payment.adapter.in.web.view;

import com.myboard.toy.common.WebAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@WebAdapter
@RequestMapping("/v2/toss")
public class PaymentController {

    @GetMapping("/success")
    public String successPage(){
        return "toss/success";
    }

    @GetMapping("/fail")
    public String failPage(){
        return "toss/fail";
    }

}
