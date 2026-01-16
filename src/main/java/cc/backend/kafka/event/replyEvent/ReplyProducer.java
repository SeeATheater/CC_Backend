package cc.backend.kafka.event.replyEvent;


import cc.backend.kafka.event.common.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReplyProducer {
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    private static final String TOPIC = "reply-created-topic";

    public void publish(ReplyEvent event) {
        if (event == null) return;

        // commentId 기준으로 파티션
        kafkaTemplate.send(
                TOPIC,
                event.commentId().toString(),
                event
        );
    }
}
