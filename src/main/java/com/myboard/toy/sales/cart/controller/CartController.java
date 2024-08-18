package com.myboard.toy.sales.cart.controller;

import com.myboard.toy.sales.cart.service.CartService;
import com.myboard.toy.sales.cartitem.service.CartItemService;
import com.myboard.toy.sales.item.service.ItemService;
import com.myboard.toy.sales.domain.Cart;
import com.myboard.toy.sales.domain.dto.CartListDto;
import com.myboard.toy.sales.domain.dto.CartTotalPriceDto;
import com.myboard.toy.sales.domain.CartItem;
import com.myboard.toy.sales.domain.dto.CartItemUpdateRequestForm;
import com.myboard.toy.sales.domain.Item;
import com.myboard.toy.security.domain.dto.AccountDto;
import com.myboard.toy.security.domain.entity.Account;
import com.myboard.toy.security.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final ItemService itemService;
    private final CartService cartService;
    private final CartItemService cartItemService;
    private final UserService userService;

    /*
       SAVE
     */

    @PostMapping("/cart/add")
    public String putItemToCart(
            @RequestParam("isbn") String isbn,
            @RequestParam("amount") int amount,
            Principal principal,
            RedirectAttributes redirectAttributes){

        // 로그인 사용자 Id
        AccountDto accountId = userService.getAccountIdByPrincipal(principal);

        Cart cart = cartService.getOrCreateCart(accountId);

        // 상품 정보 찾기
        Item item = itemService.findByIsbn(isbn);

        // 카트 가져오거나 생성
        CartItemUpdateRequestForm form = CartItemUpdateRequestForm.builder()
                .cart(cart)
                .item(item)
                .amount(amount)
                .build();

        // 카트 아이템 생성 및 저장
        CartItem cartItem = cartItemService.createCartItemOrIncreaseAmount(form);

        cartItemService.saveCartItem(cartItem);

        //장바구니에 아이템 추가
        cart.getCartItems().add(cartItem);

        redirectAttributes.addAttribute("isbn",isbn);
        return "redirect:/bookDetail";
    }

    /*
         Read
      */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/cart/list")
    private String getCartList(
            Principal principal,
            Model model){

        AccountDto accountId = userService.getAccountIdByPrincipal(principal);
        List<CartListDto> cartList = cartService.getCartList(accountId);
        CartTotalPriceDto cartTotalPrice = cartService.getCartTotalPrice(accountId);

        model.addAttribute("cartList",cartList);

        model.addAttribute("cartTotalPrice",cartTotalPrice);
        return "cart/list";
    }
}


