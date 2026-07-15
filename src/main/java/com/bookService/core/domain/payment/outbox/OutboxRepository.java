package com.bookService.core.domain.payment.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {

    /**
     * 발행 결과를 outbox 상태에 기록한다. (orderId, type) 로 대상 행을 특정한다.
     * clearAutomatically=true 로 벌크 UPDATE 후 영속성 컨텍스트를 비워 스테일 엔티티를 막는다.
     */
    @Modifying(clearAutomatically = true)
    @Query("update Outbox o set o.status = :status "
            + "where o.idempotencyKey = :idempotencyKey and o.type = :type")
    int updateStatus(@Param("idempotencyKey") String idempotencyKey,
                     @Param("type") String type,
                     @Param("status") OutboxStatus status);

    /**
     * 릴레이 대상: 아직 발행 확정되지 않은(INIT/FAILURE) 행 중, 생성된 지 threshold 이전인 것.
     * 시간 버퍼는 방금 커밋되어 AFTER_COMMIT 즉시발행이 진행 중인 행을 릴레이가 중복 발행하지
     * 않도록 하기 위함이다.
     */
    @Query("select o from Outbox o "
            + "where o.status in :statuses and o.createdAt <= :threshold "
            + "order by o.createdAt asc")
    List<Outbox> findPending(@Param("statuses") List<OutboxStatus> statuses,
                             @Param("threshold") LocalDateTime threshold);
}
