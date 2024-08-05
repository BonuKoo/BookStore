package com.myboard.toy.controller.cart;

import com.myboard.toy.application.cart.CartService;
import com.myboard.toy.application.cartitem.CartItemService;
import com.myboard.toy.application.item.service.ItemService;
import com.myboard.toy.domain.cart.Cart;
import com.myboard.toy.domain.cart.dto.CartListDto;
import com.myboard.toy.domain.cart.dto.CartTotalPriceDto;
import com.myboard.toy.domain.cartitem.CartItem;
import com.myboard.toy.domain.item.Item;
import com.myboard.toy.securityproject.domain.dto.AccountDto;
import com.myboard.toy.securityproject.domain.entity.Account;
import com.myboard.toy.securityproject.users.service.UserService;
import lombok.RequiredArgsConstructor;
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
    private final UserService userService;
    private final CartService cartService;
    private final CartItemService cartItemService;


    /*
       SAVE
     */

    @PostMapping("/cart/add")
    public String putItemToCart(
            @RequestParam("isbn") String isbn,
            @RequestParam("amount") int amount,
            Principal principal,
            RedirectAttributes redirectAttributes){

        // 로그인 사용자 정보
        Account account = getAccountByPrinciple((UsernamePasswordAuthenticationToken) principal);

        // 상품 정보 찾기
        Item item = itemService.findByIsbn(isbn);

        // 카트 가져오거나 생성
        Cart cart = cartService.getOrCreateCart(account);

        // 카트 아이템 생성 및 저장
        CartItem cartItem = cartItemService.createCartItemOrIncreaseAmount(cart, item, amount);

        cartItemService.saveCartItem(cartItem);

        //장바구니에 아이템 추가
        cart.getCartItems().add(cartItem);

        redirectAttributes.addAttribute("isbn",isbn);
        return "redirect:/bookDetail";
    }

    /*

         Read

      */

    //TODO : Cart의 tot Price를 따로 빼서 변수에 model에 담고
    // 한번만 출력하도록 바꿔야 함
    @GetMapping("/cart/list")
    private String getCartList(
            Principal principal,
            Model model){

        Account account = getAccountByPrinciple((UsernamePasswordAuthenticationToken) principal);
        List<CartListDto> cartList = cartService.getCartList(account);
        CartTotalPriceDto cartTotalPrice = cartService.getCartTotalPrice(account);

        model.addAttribute("cartList",cartList);
        model.addAttribute("cartTotalPrice",cartTotalPrice);
        return "cart/list";
    }

    private Account getAccountByPrinciple(UsernamePasswordAuthenticationToken principal) {
        UsernamePasswordAuthenticationToken authenticationToken = principal;
        AccountDto accountDto = (AccountDto) authenticationToken.getPrincipal();
        String username = accountDto.getUsername();

        // username을 토대로 account 값을 db에서 조회한다.
        Account account = userService.getAccountByUsername(username);
        return account;
    }

}


