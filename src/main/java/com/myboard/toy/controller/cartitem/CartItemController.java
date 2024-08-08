package com.myboard.toy.controller.cartitem;

import com.myboard.toy.application.cart.CartService;
import com.myboard.toy.application.cartitem.CartItemService;
import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.domain.cartitem.dto.CartItemUpdateAmountRequestForm;
import com.myboard.toy.domain.cartitem.dto.CartItemUpdateRequestForm;
import com.myboard.toy.securityproject.domain.entity.Account;
import com.myboard.toy.securityproject.utils.AccountUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class CartItemController {


    private final CartItemService cartItemService;
    private final CartService cartService;
    private final AccountUtils accountUtils;


    /*
        * CartItem id로 하나 조회
     */

    // 수량 업데이트
    //@PostMapping("/updateCartItem")
    public String updateCartItemAmount(
            Principal principal,
            @RequestParam("isbn") String isbn,
            @RequestParam("amount") int amount
    ){
        Account account = accountUtils.getAccountByPrincipal((UsernamePasswordAuthenticationToken) principal);

        Cart cart = cartService.findCartByAccount(account);

        CartItemUpdateAmountRequestForm form = CartItemUpdateAmountRequestForm.builder()
                .itemIsbn(isbn)
                .amount(amount)
                .build();

        cartItemService.updateCartItemAmount(cart,form);
        return "redirect:/cart/list";
    }

    /* 업데이트 */
    //@PostMapping("/increaseItem")
    public String increaseAmountWhenPushTheButton(
            Principal principal,
            @RequestParam("amount") int amount,
            @RequestParam("itemIsbn") String itemIsbn
    ) {
        Account account = accountUtils.getAccountByPrincipal((UsernamePasswordAuthenticationToken) principal);
        Cart cart = cartService.findCartByAccount(account);

        CartItemUpdateAmountRequestForm form = CartItemUpdateAmountRequestForm.builder()
                .itemIsbn(itemIsbn)
                .amount(amount + 1)
                .build();

        cartItemService.updateCartItemAmount(cart, form);

        return "redirect:/cart/list";
    }

    @PostMapping("/decreaseItem")
    public String decreaseAmountWhenPushTheButton(
            Principal principal,
            @RequestParam("amount") int amount,
            @RequestParam("itemIsbn") String itemIsbn
    ) {
        Account account = accountUtils.getAccountByPrincipal((UsernamePasswordAuthenticationToken) principal);
        Cart cart = cartService.findCartByAccount(account);

        CartItemUpdateAmountRequestForm form = CartItemUpdateAmountRequestForm.builder()
                .itemIsbn(itemIsbn)
                .amount(Math.max(0, amount - 1))  // 수량은 0 이하로 내려가지 않도록
                .build();

        cartItemService.updateCartItemAmount(cart, form);

        return "redirect:/cart/list";
    }
}
