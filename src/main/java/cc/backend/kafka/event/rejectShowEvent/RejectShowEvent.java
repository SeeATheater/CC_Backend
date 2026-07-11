package cc.backend.kafka.event.rejectShowEvent;

import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.event.common.enums.DomainEventType;

import java.util.UUID;

public record RejectShowEvent(
        String eventId,
        Long amateurShowId,
        Long performerId,
        String rejectReason
) implements DomainEvent {

    public static RejectShowEvent create(
            Long amateurShowId,
            Long performerId,
            String rejectReason
    ) {
        return new RejectShowEvent(
                UUID.randomUUID().toString(),
                amateurShowId,
                performerId,
                rejectReason
        );
    }

    @Override
    public DomainEventType getEventType(){
        return DomainEventType.SHOW_REJECTED;
    }
}
