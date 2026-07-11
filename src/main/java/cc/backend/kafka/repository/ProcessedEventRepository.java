package cc.backend.kafka.repository;

import cc.backend.kafka.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Kafka 메시지를 중복 처리하지 않기 위한 처리 이력 저장소
// 처리 이력을 먼저 선점하고, 이미 존재하면 에러 없이 중복 이벤트를 무시한다.
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByEventIdAndConsumerGroup(String eventId, String consumerGroup);

    @Modifying
    @Query(value = """
          INSERT IGNORE INTO processed_event
              (event_id, consumer_group, created_at)
          VALUES
              (:eventId, :consumerGroup, NOW())
          """, nativeQuery = true)
    int insertIfAbsent(
            @Param("eventId") String eventId,
            @Param("consumerGroup") String consumerGroup
    );
    // return값이 1이면 새 이벤트라 insert, 0 이면 중복 이벤트라 무시
}


