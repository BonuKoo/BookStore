package com.myboard.toy.payment.adapter.in.web.view;

import com.myboard.toy.common.WebAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@WebAdapter
@RequestMapping("/v2/toss")
public class CheckoutController {

    @GetMapping("")
    public String successPage(@RequestParam("orderId") Long orderId, Model model){

        model.addAttribute("orderId",orderId);
        return "toss/checkout";
    }
}
