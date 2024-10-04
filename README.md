# BookStore

## 프로젝트 개요

### 네이버 API를 통해 도서 정보를 제공하고, 장바구니 기능과 토스 API를 통한 결제 기능을 제공하는 이커머스 사이트입니다.
### 또한 관리자 페이지를 제공합니다.
## 기술 스택

- 프론트엔드 :: HTML, JavaScript, Thymeleaf
- 백엔드 :: Java, Spring Boot, Spring Data JPA, Query DSL
- 데이터베이스 :: H2

## 기능 설명
- **글 작성** :: CRUD 게시판입니다.
- **관리자 기능** :: Spring Security URL Mapping 및 회원 등급을 객체화로 관리할 수 있습니다.
- **도서 정보 열람** :: 네이버 API를 통해 상품 정보를 확인할 수 있습니다.
- **장바구니 기능** :: 상품을 장바구니에 담고 수량을 추가 및 제거 등을 할 수 있습니다.
- **결제** :: TOSS API를 통해 결제가 가능합니다.

### 요구 사항
- Java 17 이상
- Gradle
- H2

### 환경 설정
application.yml 파일에서 데이터베이스 설정을 맞춰야 합니다.
![(2) 검색 결과](https://github.com/user-attachments/assets/81bc633b-66dc-48f7-87bd-9aea8903665b)
![(3)회원가입 page](https://github.com/user-attachments/assets/9760b83b-c34f-405f-8aa4-d5fadfdcb556)
![(10) 페이징](https://github.com/user-attachments/assets/d27f5788-e006-4117-bf57-c74ce5450c42)
![(12)알람2](https://github.com/user-attachments/assets/7c352243-0a51-4518-b3e8-aab942f49c98)
![(1)home-로그인X](https://github.com/user-attachments/assets/5c4729ec-116f-49a2-b186-4d4a45f4cd8c)
