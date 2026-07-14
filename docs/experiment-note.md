# 실험 브랜치: feat.cart-mysql-dev — MySQL 장바구니 개발판 + 결제 정리 배치

`archive/dev-versions`에서 분기한 비교 실험용 브랜치. (MongoDB 실험 코드는 제거)

## 실험 내용
- MySQL 기반 장바구니 개발 버전: 운영 `domain/cart`와 별도로 실험한 서비스/컨트롤러.
- 결제 데이터 정리(cleanup) 서비스 개발판.

## 핵심 파일
- `core/test/cart/mysql/controller/CartMySqlControllerForDev.java`
- `core/test/cart/mysql/service/CartMySqlServiceForDev.java`
- `core/test/cart/mysql/service/PaymentCleanupServiceForDev.java`
- `src/test/.../test/cart/mysql/service/PaymentCleanupServiceTest.java`
- `core/test/cart/exception/CartItemNotExistException.java`

## 비교 대상
MongoDB 장바구니 실험은 `feat.cart-mongo-v1` / `feat.cart-mongo-v2` 브랜치 참조. 원본 전체는 `archive/dev-versions`.
