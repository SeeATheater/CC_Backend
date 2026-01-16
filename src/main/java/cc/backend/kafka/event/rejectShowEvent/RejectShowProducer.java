package cc.backend.kafka.event.rejectShowEvent;


import cc.backend.kafka.event.common.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RejectShowProducer {

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    private static final String TOPIC = "reject-show-topic";

    public void publish(RejectShowEvent event) {
        if (event == null) return;

        // 공연 ID 기준으로 파티션
        kafkaTemplate.send(
                TOPIC,
                event.amateurShowId().toString(),
                event
        );
    }
}
