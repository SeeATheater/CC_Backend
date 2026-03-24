package cc.backend.kafka.event.approvalShowEvent;

import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.event.common.enums.DomainEventType;

public record ApprovalShowEvent(
        Long amateurShowId,
        Long performerId
) implements DomainEvent {

    @Override
    public DomainEventType getEventType() {
        return DomainEventType.SHOW_APPROVED;
    }
}

