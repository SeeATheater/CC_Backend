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
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Outbox Relay의 Kafka 발행 실패 처리를 검증하는 단위 테스트.
 *
 * <p>Repository와 KafkaTemplate은 Mock이며 DB와 Kafka는 실행하지 않는다.</p>
 * <pre>
 * PENDING Outbox 조회
 *     -> KafkaTemplate이 발행 실패 반환
 *     -> Relay 호출마다 attempts 증가
 *     -> 최대 발행 실패 허용 횟수(3회)에 도달
 *     -> Outbox 상태가 FAILED로 변경되고 마지막 오류가 기록됨
 *
 * Worker interrupt
 *     -> 종료/취소 경로이므로 attempts를 증가시키지 않음
 *     -> Outbox는 PENDING을 유지해 다음 실행에서 재처리
 * </pre>
 */
@ExtendWith(MockitoExtension.class)
class KafkaOutboxRelayTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

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

    @Test
    @SuppressWarnings("unchecked")
    void keepsEventPendingWithoutCountingWorkerInterruptAsFailure() throws Exception {
        CommentEvent event = CommentEvent.create(1L, 2L, 3L, 4L);
        OutboxEvent outbox = OutboxEvent.pending(
                event,
                "comment-created-topic",
                "1",
                objectMapper.writeValueAsString(event)
        );
        CompletableFuture<SendResult<String, Object>> sendFuture =
                mock(CompletableFuture.class);

        when(outboxEventRepository.findPendingForPublish("PENDING"))
                .thenReturn(List.of(outbox));
        when(kafkaTemplate.send(anyString(), anyString(), any(DomainEvent.class)))
                .thenReturn(sendFuture);
        when(sendFuture.get(anyLong(), any(TimeUnit.class)))
                .thenThrow(new InterruptedException("worker shutdown"));

        try {
            relay.publishPendingEvents();

            assertThat(Thread.currentThread().isInterrupted()).isTrue();
            assertThat(outbox.getAttempts()).isZero();
            assertThat(outbox.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(outbox.getLastError()).isNull();
        } finally {
            // Relay가 복구한 interrupt flag가 다른 테스트에 전파되지 않도록 정리한다.
            Thread.interrupted();
        }
    }
}
