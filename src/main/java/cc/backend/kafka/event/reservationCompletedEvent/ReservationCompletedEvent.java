package cc.backend.kafka.event.reservationCompletedEvent;

import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.event.common.enums.DomainEventType;

import java.util.UUID;

public record ReservationCompletedEvent(
        String eventId,
        Long amateurShowId,
        Long realTicketId,
        Long memberId
)   implements DomainEvent {

    public static ReservationCompletedEvent create(
            Long amateurShowId,
            Long realTicketId,
            Long memberId
    ) {
        return new ReservationCompletedEvent(
                UUID.randomUUID().toString(),
                amateurShowId,
                realTicketId,
                memberId
        );
    }

    @Override
    public DomainEventType getEventType() {
        return DomainEventType.TICKET_RESERVATION_COMPLETED;
    }
}
