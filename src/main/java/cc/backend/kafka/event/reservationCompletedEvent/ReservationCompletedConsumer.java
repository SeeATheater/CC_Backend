package cc.backend.kafka.event.reservationCompletedEvent;

import cc.backend.kafka.event.rejectShowEvent.RejectShowEvent;
import cc.backend.notice.service.NoticeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ReservationCompletedConsumer {

    private final NoticeService noticeService;

    @KafkaListener(
            topics = "reservation-completed-topic",
            groupId = "reservation-completed-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(ReservationCompletedEvent event) {
        if (event == null) return;

        noticeService.notifyTicketReservation(event);
    }
}
