package cc.backend.kafka.entity;

import cc.backend.domain.common.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "processed_event",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_processed_event",
                columnNames = {"event_id", "consumer_group"}
        ) //한 event를 여러 consumer-group이 소비할 수 있음 -> consumer_group도 unique 제약에 포함
)
public class ProcessedEvent extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(name = "consumer_group", nullable = false)
    private String consumerGroup;

}
