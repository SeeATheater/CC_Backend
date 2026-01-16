package cc.backend.kafka.event.reservationCompletedEvent;

import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.event.common.enums.DomainEventType;


public record ReservationCompletedEvent(
    Long amateurShowId,
    Long realTicketId,
    Long memberId       //예약자
)   implements DomainEvent {

    @Override
    public DomainEventType getEventType() {
        return DomainEventType.TICKET_RESERVATION_COMPLETED;
    }
}
