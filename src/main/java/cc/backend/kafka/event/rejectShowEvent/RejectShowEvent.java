package cc.backend.kafka.event.rejectShowEvent;

import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.event.common.enums.DomainEventType;


public record RejectShowEvent(
        Long amateurShowId,
        Long performerId,
        String rejectReason
) implements DomainEvent {

    @Override
    public DomainEventType getEventType(){
        return DomainEventType.SHOW_REJECTED;
    }
}
