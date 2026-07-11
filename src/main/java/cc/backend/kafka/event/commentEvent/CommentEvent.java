package cc.backend.kafka.event.commentEvent;

import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.event.common.enums.DomainEventType;

import java.util.UUID;


public record CommentEvent (
        String eventId,
        Long boardId,
        Long boardWriterId, //게시물 작성자 id
        Long commentId,
        Long commentWriterId //댓글 작성자 id
)  implements DomainEvent {

    public static CommentEvent create(
            Long boardId,
            Long boardWriterId,
            Long commentId,
            Long commentWriterId
    ) {
        return new CommentEvent(
                UUID.randomUUID().toString(),
                boardId,
                boardWriterId,
                commentId,
                commentWriterId
        );
    }

    @Override
    public DomainEventType getEventType() {
        return DomainEventType.COMMENT_ON_BOARD;
    }

}
