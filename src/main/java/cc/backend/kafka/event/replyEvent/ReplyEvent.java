package cc.backend.kafka.event.replyEvent;

import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.event.common.enums.DomainEventType;

import java.util.UUID;

public record ReplyEvent (
        String eventId,
        Long commentId,
        Long commentWriterId,
        Long replyId,
        Long replyWriterId
) implements DomainEvent {

    public static ReplyEvent create(
            Long commentId,
            Long commentWriterId,
            Long replyId,
            Long replyWriterId
    ) {
        return new ReplyEvent(
                UUID.randomUUID().toString(),
                commentId,
                commentWriterId,
                replyId,
                replyWriterId
        );
    }

    @Override
    public DomainEventType getEventType() {
        return DomainEventType.REPLY_ON_COMMENT;
    }
}
