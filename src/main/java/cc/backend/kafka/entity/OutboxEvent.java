package cc.backend.kafka.entity;

import cc.backend.domain.common.BaseEntity;
import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.event.common.enums.DomainEventType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "outbox_event",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_outbox_event_id",
                columnNames = "event_id"
        ),
        indexes = @Index(
                name = "idx_outbox_status_id",
                columnList = "status, id"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(nullable = false)
    private String topic;

    @Column(name = "event_key", nullable = false)
    private String eventKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DomainEventType eventType;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    private int attempts;
    private LocalDateTime publishedAt;

    @Column(length = 1000)
    private String lastError;

    //pending 상태 OutboxEvent 생성
    public static OutboxEvent pending(
            DomainEvent event,
            String topic,
            String key,
            String payload
    ) {
        OutboxEvent outbox = new OutboxEvent();
        outbox.eventId = event.eventId();
        outbox.eventType = event.getEventType();
        outbox.topic = topic;
        outbox.eventKey = key;
        outbox.payload = payload;
        outbox.status = OutboxStatus.PENDING;
        return outbox;
    }

    //published로 상태 변경
    public void markPublished() {
        status = OutboxStatus.PUBLISHED;
        publishedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage, int maxAttempts) {
        attempts++;
        lastError = truncate(errorMessage);

        if (attempts >= maxAttempts) {
            status = OutboxStatus.FAILED;
        }
    }

    private String truncate(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }
        return errorMessage.length() <= 1000
                ? errorMessage
                : errorMessage.substring(0, 1000);
    }

}
