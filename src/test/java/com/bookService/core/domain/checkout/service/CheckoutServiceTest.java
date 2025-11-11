package com.bookService.core.domain.checkout.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

//@SpringBootTest
//@TestPropertySource(properties = {"jwt.secret=FlRpX30pMqDbiAkmlfArbrmVkDD4RqISskGZmBFax5oGVxzXXWUzTR5JyskiHMIV9M1Oicegkpi46AdvrcX1E6CmTUBc6IFbTPiD"})
//@Import(JwtConfig.class)
public class CheckoutServiceTest {

    /**
     * 검증 포인트
     * 1. PaymentEvent가 중복 생성되지 않는가
     * 2. 총 결제 금액과 주문 이름이 정확하게 누적되는가
     * 3. CartItem과 PaymentOrder 간 관계가 올바르게 저장되는가
     * */
/*
    @Autowired private CheckoutService checkoutService;
    @Autowired private CartService cartService;
    @Autowired private SpringDataJpaPaymentEventRepository paymentEventRepository;
    @Autowired private CartItemRepository cartItemRepository;

    private Long testUserId1;

    private List<CartListDTOForQueryProjection> cartItemList;
    private CheckoutCommand command;
    */
    /*
    @BeforeEach
    void setUp(){
        // 데이터 준비
        String userId = "1030";
        List<CartListDTOForQueryProjection> cartItemList = cartService.getCartItemList(userId);

        List<Long> cartItemIds = cartItemList.stream()
                .map(CartListDTOForQueryProjection::getCartItemId)
                .toList();

        CheckoutRequest checkoutRequest = new CheckoutRequest();
        checkoutRequest.setCartItemIds(cartItemIds);

        String idempotencyKey = IdempotencyCreator.create(checkoutRequest);
        Long cartId = cartService.findCartByAccountIdStringType(userId);

        command = CheckoutCommand.builder()
                .cartId(cartId)
                .buyerId(Long.parseLong(userId))
                .cartItemIds(checkoutRequest.getCartItemIds())
                .idempotencyKey("test-key"+IdempotencyCreator.create(checkoutRequest))
                .build();
    }
    */
    /*

    @Test
    void concurrentCheckout_shouldMaintainConsistency() {
        int threadCount = 1;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<CompletableFuture<CheckoutResult>> futures = new ArrayList<>();


        for (int i = 0; i < threadCount; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> checkoutService.checkout2(command), executor));
        }

        List<CheckoutResult> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // 모든 결과의 orderId가 동일하고, 금액이 일치해야 함
        String expectedOrderId = results.get(0).getOrderId();
        Long expectedAmount = results.get(0).getAmount();

        for (CheckoutResult result : results) {
            assertEquals(expectedOrderId, result.getOrderId());
            assertEquals(expectedAmount, result.getAmount());
        }
    }
     */
/*
    @Test
    void concurrentCheckout_shouldMaintainConsistency2() {

        List<CompletableFuture<CheckoutResult>> futures = IntStream.range(0, 10)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return checkoutService.checkout(command);
                    } catch (DataIntegrityViolationException e) {
                        // ✅ 중복 발생하면 테스트에서는 정상 처리
//                        System.out.println("중복 발생, 정상 처리로 간주: " + e.getMessage());
                        return null; // 혹은 원하는 dummy CheckoutResult
                    }
                }))
                .collect(Collectors.toList());

        List<CheckoutResult> results = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull) // null 제외
                .collect(Collectors.toList());

        // 정상적으로 commit된 CheckoutResult가 하나는 존재하는지 확인
        assertFalse(results.isEmpty(), "최소한 하나의 CheckoutResult는 있어야 합니다.");
    }
*/
}