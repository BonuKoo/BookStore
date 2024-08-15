package com.myboard.toy.sales.cartitem.controller;

import com.myboard.toy.sales.cart.service.CartService;
import com.myboard.toy.sales.cartitem.service.CartItemService;
import com.myboard.toy.sales.domain.dto.CartDto;
import com.myboard.toy.sales.domain.dto.CartItemRemoveRequestForm;
import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;
    private final CartService cartService;
    private final UserService userService;

    /* DELETE */

    @PostMapping("/removeItem")
    public String deleteCartItem(
            Principal principal,
            @RequestParam("itemIsbn") String itemIsbn,
            RedirectAttributes redirectAttributes){

        try {

            AccountDto accountId = userService.getAccountIdByPrincipal(principal);

            CartDto cartDto = cartService.getCartIdByAccountId(accountId);
            Long cartId = cartDto.getId();
            CartItemRemoveRequestForm form = CartItemRemoveRequestForm.builder()
                    .cartId(cartId)
                    .itemIsbn(itemIsbn)
                    .build();
            cartItemService.removeCartItem(form);

            redirectAttributes.addFlashAttribute("message", "Item removed successfully.");
            return "redirect:/cart/list";
        } catch (NoSuchElementException e) {
            // 실패 메시지를 추가하고 리다이렉트합니다
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart/list";
        } catch (Exception e) {
            e.printStackTrace();
            // 실패 메시지를 추가하고 리다이렉트합니다
            redirectAttributes.addFlashAttribute("error", "Failed to remove item.");
            return "redirect:/cart/list";
        }
    }
}
