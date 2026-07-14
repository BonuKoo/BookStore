# 실험 브랜치: feat.checkout-lock-dev — 체크아웃 개발 버전 + 재고 락 전략 실험

`archive/dev-versions`에서 분기한 비교 실험용 브랜치. (장바구니 실험 코드는 제거)

## 실험 내용
1. **체크아웃 서비스 개발 버전들** — 운영 `domain/checkout/CheckoutService`로 수렴하기 전의 시행착오:
   - `CheckoutServiceForDev` — TransactionTemplate 수동 트랜잭션 + 비동기 재고 차감/보상 실험
   - `CheckoutServiceForDev2` — 낙관/비관 락 파사드를 조합한 버전
   - `CheckoutFindExistingOrderServiceForDev`, `CheckoutTransactionRepository`(중복 주문 멱등 처리),
     `CheckoutItemCompensativeService`, `CheckoutReadService`, `RestCheckoutControllerForDev`
2. **재고 동시성 락 전략 비교**:
   - `core/test/item/OptimisticLockItemService` (+ `StockUnderflowException`, 재시도 파사드 `facade/OptimisticLockStockFacade`)
   - `core/test/item/PessimisticLockItemService` (+ `facade/PessimisticLockStockFacade`)
   - 검증 테스트: `OptimisticLockStockFacadeTest`, `PessimisticLockItemServiceTest`

두 실험은 상호 의존(체크아웃 개발판이 락 파사드를 사용)이라 하나의 브랜치로 묶었다.

## 비교 대상
장바구니 실험은 `feat.cart-mongo-v1/v2`, `feat.cart-mysql-dev` 참조. 원본 전체는 `archive/dev-versions`.
