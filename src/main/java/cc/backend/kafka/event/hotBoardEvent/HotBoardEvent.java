package cc.backend.kafka.event.hotBoardEvent;

import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.event.common.enums.DomainEventType;

public record HotBoardEvent(
    Long boardId,
    Long writerId
) implements DomainEvent {

    @Override
    public DomainEventType getEventType() {
        return DomainEventType.HOT_BOARD_BECAME;
    }
}
