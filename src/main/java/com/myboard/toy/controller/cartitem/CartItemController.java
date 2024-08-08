package com.myboard.toy.controller.cartitem;

import com.myboard.toy.application.cart.CartService;
import com.myboard.toy.application.cartitem.CartItemService;
import com.myboard.toy.domain.cartitem.dto.CartItemRemoveRequestForm;
import com.myboard.toy.securityproject.domain.entity.Account;
import com.myboard.toy.securityproject.utils.AccountUtils;
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
    private final AccountUtils accountUtils;

    /* DELETE */

    @PostMapping("removeItem")
    public String deleteCartItem(
            Principal principal,
            @RequestParam("itemIsbn") String itemIsbn,
            RedirectAttributes redirectAttributes){

        try {
            Account account = accountUtils.getAccountByPrincipal((UsernamePasswordAuthenticationToken) principal);
            Long cartId = account.getCart().getId();

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
