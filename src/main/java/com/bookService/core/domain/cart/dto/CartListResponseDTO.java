package com.bookService.core.domain.cart.dto;

import com.bookService.core.domain.cartitem.dto.CartListDTOForQueryProjection;
import com.bookService.core.domain.cartitem.dto.CartTotalPriceDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartListResponseDTO {

    private List<CartListDTOForQueryProjection> cartList;
    private CartTotalPriceDto totalPrice;

}
