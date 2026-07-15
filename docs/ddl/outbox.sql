-- M1 Transactional Outbox — 수동 적용 DDL
-- 대상 DB: core-spa MySQL (spring.jpa.hibernate.ddl-auto=none 이므로 애플리케이션이 스키마를 만들지 않는다)
-- 적용: 결제/주문 스키마가 있는 동일 스키마에 아래를 1회 실행한다.
--
-- 설계 메모:
--  - idempotency_key = orderId. 주문당 terminal 결제 상태(SUCCESS/FAILURE)는 하나뿐이라 UNIQUE가 성립한다.
--  - payload/metadata 는 JSON 컬럼 (Jackson 직렬화 문자열 저장).
--  - status: INIT(저장만) / SUCCESS(발행 확정) / FAILURE(발행 실패, 릴레이 재발행 대상).
--  - Kafka partitionKey 컬럼은 RabbitMQ 재구현에서 제거됨.

CREATE TABLE IF NOT EXISTS outbox (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    idempotency_key VARCHAR(255) NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    type            VARCHAR(40)  NOT NULL,
    payload         JSON         NOT NULL,
    metadata        JSON         NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_outbox_idempotency_key UNIQUE (idempotency_key),
    -- 릴레이 조회(status IN (INIT,FAILURE) AND created_at <= ?) 가속용 복합 인덱스
    KEY idx_outbox_status_created_at (status, created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
