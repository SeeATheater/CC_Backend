package cc.backend.kafka.event.approvalShowEvent;

import cc.backend.kafka.event.common.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApprovalShowProducer {
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;
    private static final String TOPIC = "approval-show-topic";

    /**
     * 승인된 공연 이벤트 발행
     * @param event ApprovalShowEvent
     */
    public void publish(ApprovalShowEvent event) {
        if (event == null) return;

        // amateurShowId 기준 파티션
        kafkaTemplate.send(TOPIC, event.amateurShowId().toString(), event);
    }

}
