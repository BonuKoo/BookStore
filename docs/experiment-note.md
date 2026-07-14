# 실험 브랜치: feat.cart-mongo-v1 — MongoDB 장바구니 (Map 문서 구조)

`archive/dev-versions`에서 분기한 비교 실험용 브랜치.

## 실험 내용
장바구니를 MongoDB 단일 문서로 모델링할 때, 아이템 컬렉션을 **Map(`Map<String, ItemEntry>`) 구조**로 담는 v1 설계.
`isbn`을 키로 사용해 아이템 조회/수정이 O(1)이라는 가설을 검증.

## 핵심 파일
- `core/test/cart/mongo/document/CartModelMapDocument1.java` — Map 기반 장바구니 문서
- `core/test/cart/mongo/document/ItemEntry1.java` — Map의 값 타입
- `core/test/cart/mongo/repository/CartModelMapRepository1.java`
- `core/test/cart/mongo/service/CartMongoServiceTest1.java`
- `core/test/cart/mongo/controller/CartMongoControllerTest1.java`

## 비교 대상
Array 문서 구조(v2)는 `feat.cart-mongo-v2` 브랜치 참조. 두 버전이 모두 포함된 원본은 `archive/dev-versions`.
