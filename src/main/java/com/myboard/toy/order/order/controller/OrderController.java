package com.myboard.toy.order.order.controller;

import com.myboard.toy.order.domain.Address;
import com.myboard.toy.order.domain.Delivery;
import com.myboard.toy.order.domain.Order;
import com.myboard.toy.order.domain.OrderItem;
import com.myboard.toy.order.domain.status.DeliveryStatus;
import com.myboard.toy.sales.cart.service.CartService;
import com.myboard.toy.sales.domain.Cart;
import com.myboard.toy.sales.domain.CartItem;
import com.myboard.toy.sales.item.service.ItemService;
import com.myboard.toy.order.order.service.OrderService;
import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.eclipse.angus.mail.imap.protocol.MODSEQ;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;

    @GetMapping("/orderForm")
    public String showOrderForm(
            @RequestParam("selectedItems") List<String> selectedItems,
            Model model,
            Principal principal){

        AccountDto accountId = userService.getAccountIdByPrincipal(principal);
        Cart cart = cartService.getOrCreateCart(accountId);

        List<CartItem> selectedCartItems = cart.getCartItems().stream()
                .filter(item -> selectedItems.contains(item.getItem().getIsbn()))
                .toList();

        String address = userService.getAddress(accountId);

        // 선택된 아이템들의 총 가격 계산
        int totalPrice = selectedCartItems.stream()
                .mapToInt(item -> item.getItem().getPrice() * item.getCount())
                .sum();


        model.addAttribute("cartList", selectedCartItems);
        //총 가격 계산
        model.addAttribute("cartTotalPrice", totalPrice);
        model.addAttribute("address", address);

        return "order/orderForm";
    }

    @PostMapping("/submit")
    public String submitOrder(
            @RequestParam("address") String addressString,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        AccountDto accountId = userService.getAccountIdByPrincipal(principal);
        Cart cart = cartService.getOrCreateCart(accountId);

        //Address address = parseAddress(addressString);

        Delivery delivery = Delivery.builder()
                .status(DeliveryStatus.READY)
                .address(addressString) // 사용자가 입력한 주소를 설정
                .build();

        Order order = orderService.createOrderFromCart(accountId, cart, delivery);

        redirectAttributes.addAttribute("orderId",order.getId());

        //주문 ID를 URL 파라미터로 전달
        return "redirect:/v2/toss";
    }

    /*
    private Address parseAddress(String addressString){

        String[] parts = addressString.split(", ");
        if (parts.length != 5) {
            throw new IllegalArgumentException("주소 형식이 올바르지 않습니다.");
        }
        return Address.builder()
                .postcode(parts[0])
                .roadAddress(parts[1])
                .jibunAddress(parts[2])
                .detailAddress(parts[3])
                .extraAddress(parts[4])
                .build();
    }*/
}
