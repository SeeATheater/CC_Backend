package cc.backend.notice.service;

import cc.backend.notice.event.TicketReservationCommitEvent;
import cc.backend.kafka.event.reservationCompletedEvent.ReservationCompletedEvent;
import cc.backend.kafka.event.reservationCompletedEvent.ReservationCompletedProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class ReservationCommitEventListener {
    private final ReservationCompletedProducer reservationCompletedProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationCommit(TicketReservationCommitEvent event) {

        // TempTicket 생성 트랜잭션 커밋 완료 후 Kafka 이벤트 발송
        reservationCompletedProducer.publish(
                new ReservationCompletedEvent(event.amateurShowId(), event.realTicketId(), event.memberId())
        );
    }
}
