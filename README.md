

# 📚 BookStore: E-commerce Payment & Auth Module

> Kakao OAuth를 통한 안전한 신원 인증 및 Toss API를 활용한 전자 상거래 결제 모듈 구현.
>
> [![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](링크)
> [![Java Version](https://img.shields.io/badge/java-17-blue)](링크)
> [![License](https://img.shields.io/badge/license-MIT-lightgrey)](LICENSE)
> 
## 🛠️ 기술 스택 (Tech Stack)

| 구분 | 기술 / 라이브러리 | 역할 |
| :--- | :--- | :--- |
| **Framework** | **Spring Boot (Java 17)** | 서버 구축 및 RESTful API 구현의 핵심 프레임워크 |
| **Database** | **MySQL** | 주문, 장바구니, 사용자 정보 등 영속성 데이터 관리 |
| **Build Tool** | **Gradle** | 의존성 관리 및 프로젝트 빌드 자동화 |
| **Payment Gateway** | **Toss Payments** | 결제 승인, 취소 등 실제 금융 거래 처리 |
| **Security** | **Spring Security, JWT** | API 접근 통제 및 사용자 권한 관리 |

## 주요 기능
## ✨ 주요 기능 상세 (Key Features)

### 1. 결제 모듈 (Toss Payments 연동)

* **💳 결제 승인 로직 흐름 구현:**
    * Toss Payments **결제 승인 API**를 통한 실제 거래 처리.
    * 결제 승인 전후의 **DB 트랜잭션** 관리 및 결제 상태 업데이트.
* **🔄 중복 주문 방지를 위한 멱등성 (Idempotency) 구현:**
    * 결제 요청 시 **고유 키(e.g., Order ID)**를 활용하여 중복 결제를 시스템적으로 방지.

### 2. 사용자 인증 및 보안

* **🔑 KAKAO API를 활용한 OAuth 2.0:**
    * 카카오 로그인을 통한 **회원가입 및 사용자 인증 절차 간소화**.
    * 카카오 API 서버와의 **인가 코드/토큰 교환** 플로우 구현.
* **🛡️ JWT (JSON Web Token) 기반 인증:**
    * OAuth 인증 후, 서버에서 **JWT 토큰**을 발급하여 **Stateless한 인증 환경** 구축.
    * **Spring Security Filter**를 활용한 모든 API 요청에 대한 토큰 유효성 검증.

### 3. 쇼핑 및 주문 관리

* **🛒 장바구니 (Shopping Cart) 관리:**
    * 사용자별 장바구니 상품 **추가/삭제/수량 변경** CRUD API 제공.
    * 장바구니 항목의 **가격 및 재고 유효성 검증** 로직 포함.

### 4. 주문 생성 및 연동 (Order Integration)

* **🔗 주문-결제 연동 흐름 제공:**
    * 장바구니 상품을 기반으로 **주문 객체를 생성**하고, 이를 결제 모듈로 전달하여 **결제를 요청**하는 End-to-End 흐름 구현.
* **🔒 재고 확인 및 Locking 처리 로직:**
    * 주문 생성 시점에 **동시성 문제**를 방지하기 위해 **낙관적 Locking**을 활용한 **재고 차감** 로직 구현.
