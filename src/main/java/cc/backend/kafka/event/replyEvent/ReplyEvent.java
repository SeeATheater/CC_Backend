package cc.backend.kafka.event.replyEvent;

import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.event.common.enums.DomainEventType;

public record ReplyEvent (
    Long commentId,
    Long commentWriterId,       //댓글 작성자 id
    Long replyId,               // 대댓글 commentId
    Long replyWriterId          //대댓글 작성자 id
) implements DomainEvent {

    @Override
    public DomainEventType getEventType() {
        return DomainEventType.REPLY_ON_COMMENT;
    }
}

