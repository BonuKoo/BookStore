package com.myboard.toy.controller.cart;

import com.myboard.toy.application.account.AccountService;
import com.myboard.toy.application.cart.CartService;
import com.myboard.toy.application.item.service.ItemService;
import com.myboard.toy.common.exception.ItemNotFoundException;
import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.domain.cartitem.CartItem;
import com.myboard.toy.domain.item.Item;
import com.myboard.toy.securityproject.domain.dto.AccountDto;
import com.myboard.toy.securityproject.domain.entity.Account;
import com.myboard.toy.securityproject.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final ItemService itemService;
    private final UserService userService;
    private final CartService cartService;

    @PostMapping("/cart/add")
    public String putItemToCart(
            @RequestParam("isbn") String isbn,
            @RequestParam("amount") int amount,
            Principal principal) {

        // 현재 로그인한 사용자 정보 가져오기
        Account account = getAccountByPrinciple((UsernamePasswordAuthenticationToken) principal);

// 아이템 조회
        Item item = itemService.findByIsbn(isbn);
        if (item == null) {
            throw new ItemNotFoundException("아이템을 찾을 수 없습니다.");
        }

        // 카트 조회
        Cart cart = cartService.findCartByAccount(account);
        if (cart == null) {
            cart = Cart.createCart(account);
        }

        // 카트 아이템 조회
        CartItem cartItem = cart.getCartItems().stream()
                .filter(ci -> ci.getItem().equals(item))
                .findFirst()
                .orElse(null);

        if (cartItem != null) {
            cartItem.addCount(amount);
        } else {
            cartItem = CartItem.createCartItem(cart, item, amount);
            cart.getCartItems().add(cartItem);
        }

        cartService.save(cart);

        return "redirect:/cart"; // 카트 페이지로 리다이렉트
    }

    private Account getAccountByPrinciple(UsernamePasswordAuthenticationToken principal) {
        UsernamePasswordAuthenticationToken authenticationToken = principal;
        AccountDto accountDto = (AccountDto) authenticationToken.getPrincipal();
        String username1 = accountDto.getUsername();
        String username = username1;

        // username을 토대로 account 값을 db에서 조회한다.
        Account account = userService.getAccountByUsername(username);
        return account;
    }

}


