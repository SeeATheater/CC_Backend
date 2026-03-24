package cc.backend.kafka.event.reservationCompletedEvent;


import cc.backend.kafka.event.common.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationCompletedProducer {
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    private static final String TOPIC = "reservation-completed-topic";


    public void publish(ReservationCompletedEvent event) {
        if (event == null) return;

        // memberId 기준 파티션
        kafkaTemplate.send(
                TOPIC,
                event.memberId().toString(),
                event
        );
    }
}

