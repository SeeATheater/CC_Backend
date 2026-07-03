package cc.backend.kafka.event.hotBoardEvent;

import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.event.common.enums.DomainEventType;

import java.util.UUID;

public record HotBoardEvent(
        String eventId,
        Long boardId,
        Long writerId
) implements DomainEvent {

    public static HotBoardEvent create(Long boardId, Long writerId) {
        return new HotBoardEvent(
                UUID.randomUUID().toString(),
                boardId,
                writerId
        );
    }

    @Override
    public DomainEventType getEventType() {
        return DomainEventType.HOT_BOARD_BECAME;
    }
}
