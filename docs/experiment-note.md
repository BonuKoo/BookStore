# 실험 브랜치: feat.cart-mongo-v2 — MongoDB 장바구니 (Array 문서 구조)

`archive/dev-versions`에서 분기한 비교 실험용 브랜치.

## 실험 내용
장바구니를 MongoDB 단일 문서로 모델링할 때, 아이템 컬렉션을 **Array(`List<CartItemModelArrayDocument2>`) 구조**로 담는 v2 설계.
Map(v1) 대비 인덱싱/쿼리 유연성과 문서 구조 가독성을 비교 검증.

## 핵심 파일
- `core/test/cart/mongo/document/CartModelArrayDocument2.java` — Array 기반 장바구니 문서
- `core/test/cart/mongo/document/CartItemModelArrayDocument2.java` — 배열 요소 타입
- `core/test/cart/mongo/repository/CartModelArrayRepository2.java`
- `core/test/cart/mongo/service/CartMongoServiceTest2.java`
- `core/test/cart/mongo/controller/CartMongoControllerTest2.java`

## 참고
`CartMongoServiceTest2`가 v1의 `CartModelMapRepository1`을 참조(마이그레이션/비교 목적)하므로
v1의 document/repository 파일은 이 브랜치에도 남아 있다. 제거한 것은 v1의 service/controller.

## 비교 대상
Map 문서 구조(v1)는 `feat.cart-mongo-v1` 브랜치 참조. 두 버전이 모두 포함된 원본은 `archive/dev-versions`.
