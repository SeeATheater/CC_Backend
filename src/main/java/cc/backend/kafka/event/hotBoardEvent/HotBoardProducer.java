package cc.backend.kafka.event.hotBoardEvent;

import cc.backend.kafka.event.common.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotBoardProducer {
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;
    private static final String TOPIC = "hot-board-topic";

    public void publish(HotBoardEvent event) {
        if (event == null) return;

        // boardId 기준 파티션
        kafkaTemplate.send(
                TOPIC,
                event.boardId().toString(),
                event
        );
    }
}
