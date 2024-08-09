package com.myboard.toy.infrastructure.cart;

import com.myboard.toy.sales.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CartRepository4QueryDslImplTest {

    @Autowired
    private CartRepository cartRepository;

    /*
    @Test
    public void testGetCartList(){
        //GIVEN DB에 이미 값들은 있으므로 DB 값을 불러온다.
        //WHEN
        List<CartListDto> cartList = cartRepository.getCartList(1L);
        //Then
        assertThat(cartList).isNotEmpty();

        // Assert item 1
        CartListDto item1Dto = cartList.stream()
                .filter(dto -> dto.getName().equals("AWS (The Most Complete Guide to Amazon Web Services from Beginners to Advanced)"))
                .findFirst().orElse(null);
        assertThat(item1Dto).isNotNull();
        assertThat(item1Dto.getPrice()).isEqualTo(54840);
        assertThat(item1Dto.getAmount()).isEqualTo(2);
        assertThat(item1Dto.getEachPrice()).isEqualTo(54840);
        assertThat(item1Dto.getTotPrice()).isEqualTo(109680);

    }
*/
}