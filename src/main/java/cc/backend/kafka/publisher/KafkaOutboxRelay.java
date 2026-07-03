package cc.backend.kafka.publisher;

import cc.backend.kafka.entity.OutboxEvent;
import cc.backend.kafka.entity.OutboxStatus;
import cc.backend.kafka.event.approvalShowEvent.ApprovalShowEvent;
import cc.backend.kafka.event.commentEvent.CommentEvent;
import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.event.hotBoardEvent.HotBoardEvent;
import cc.backend.kafka.event.rejectShowEvent.RejectShowEvent;
import cc.backend.kafka.event.replyEvent.ReplyEvent;
import cc.backend.kafka.event.reservationCompletedEvent.ReservationCompletedEvent;
import cc.backend.kafka.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaOutboxRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${outbox.max-attempts:3}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${outbox.polling-delay-ms:1000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> outboxEvents = outboxEventRepository.findPendingForPublish(
                OutboxStatus.PENDING.name()
        );

        for(OutboxEvent outboxEvent : outboxEvents) {
            try {
                DomainEvent event = deserialize(outboxEvent);

                kafkaTemplate.send(
                    outboxEvent.getTopic(),
                    outboxEvent.getEventKey(),
                    event
                ).get(5, TimeUnit.SECONDS);

                outboxEvent.markPublished();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                handleFailure(outboxEvent, e);
                return;
            } catch (Exception e) {
                handleFailure(outboxEvent, e);
            }
        }
    }

    private void handleFailure(OutboxEvent outboxEvent, Exception exception) {
        outboxEvent.markFailed(resolveRootMessage(exception), maxAttempts);

        log.error(
                "Outbox publish failed. outboxId={}, eventId={}, attempts={}, status={}",
                outboxEvent.getId(),
                outboxEvent.getEventId(),
                outboxEvent.getAttempts(),
                outboxEvent.getStatus(),
                exception
        );
    }

    private String resolveRootMessage(Exception exception) {
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }

    // Kafka 메시지 전송을 위해 이벤트를 역직렬화
    private DomainEvent deserialize(OutboxEvent outbox) throws JsonProcessingException {

        Class<? extends DomainEvent> eventClass =
                switch (outbox.getEventType()) {
                    case COMMENT_ON_BOARD -> CommentEvent.class;
                    case REPLY_ON_COMMENT -> ReplyEvent.class;
                    case SHOW_APPROVED -> ApprovalShowEvent.class;
                    case SHOW_REJECTED -> RejectShowEvent.class;
                    case TICKET_RESERVATION_COMPLETED -> ReservationCompletedEvent.class;
                    case HOT_BOARD_BECAME -> HotBoardEvent.class;
                };

        return objectMapper.readerFor(eventClass)
                .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .readValue(outbox.getPayload());
    }
}
