package cc.backend.kafka.repository;

import cc.backend.kafka.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    @Query(
        value = """
                SELECT *
                FROM outbox_event
                WHERE status = :status
                ORDER BY id ASC
                LIMIT 100
                FOR UPDATE SKIP LOCKED 
            """,
        nativeQuery = true
    )
    List<OutboxEvent> findPendingForPublish(@Param("status") String status);
}
