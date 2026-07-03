package cc.backend.notice.unit.kafka;

import cc.backend.kafka.entity.OutboxEvent;
import cc.backend.kafka.entity.OutboxStatus;
import cc.backend.kafka.event.commentEvent.CommentEvent;
import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.publisher.KafkaOutboxRelay;
import cc.backend.kafka.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Outbox Relay의 Kafka 발행 실패 처리를 검증하는 단위 테스트.
 *
 * <p>Repository와 KafkaTemplate은 Mock이며 DB와 Kafka는 실행하지 않는다.</p>
 * <pre>
 * PENDING Outbox 조회
 *     -> KafkaTemplate이 발행 실패 반환
 *     -> Relay 호출마다 attempts 증가
 *     -> 최대 재시도 횟수(3회)에 도달
 *     -> Outbox 상태가 FAILED로 변경되고 마지막 오류가 기록됨
 * </pre>
 */
@ExtendWith(MockitoExtension.class)
class KafkaOutboxRelayTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, DomainEvent> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private KafkaOutboxRelay relay;

    @BeforeEach
    void setUp() {
        relay = new KafkaOutboxRelay(
                outboxEventRepository,
                kafkaTemplate,
                objectMapper
        );
        ReflectionTestUtils.setField(relay, "maxAttempts", 3);
    }

    @Test
    void marksEventAsFailedAfterMaximumAttempts() throws Exception {
        CommentEvent event = CommentEvent.create(1L, 2L, 3L, 4L);
        OutboxEvent outbox = OutboxEvent.pending(
                event,
                "comment-created-topic",
                "1",
                objectMapper.writeValueAsString(event)
        );

        when(outboxEventRepository.findPendingForPublish("PENDING"))
                .thenReturn(List.of(outbox));
        when(kafkaTemplate.send(anyString(), anyString(), any(DomainEvent.class)))
                .thenReturn(CompletableFuture.failedFuture(
                        new IllegalStateException("Kafka unavailable")
                ));

        relay.publishPendingEvents();
        relay.publishPendingEvents();
        relay.publishPendingEvents();

        assertThat(outbox.getAttempts()).isEqualTo(3);
        assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(outbox.getLastError()).contains("Kafka unavailable");
    }
}
