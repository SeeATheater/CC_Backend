package cc.backend.kafka.event.approvalShowEvent;

import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.event.common.enums.DomainEventType;

import java.util.UUID;

public record ApprovalShowEvent(
        String eventId,
        Long amateurShowId,
        Long performerId
) implements DomainEvent {

    public static ApprovalShowEvent create(Long amateurShowId, Long performerId) {
        return new ApprovalShowEvent(
                UUID.randomUUID().toString(),
                amateurShowId,
                performerId
        );
    }

    @Override
    public DomainEventType getEventType() {
        return DomainEventType.SHOW_APPROVED;
    }
}
