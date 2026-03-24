package cc.backend.kafka.event.commentEvent;

import cc.backend.kafka.event.common.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentProducer {
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    private static final String TOPIC = "comment-created-topic";

    public void publish(CommentEvent event) {
        if (event == null) return;

        // boardId 기준 파티션
        kafkaTemplate.send(
                TOPIC,
                event.boardId().toString(),
                event
        );
    }

}
