package cc.backend.kafka.event.commentEvent;

import cc.backend.kafka.event.common.DomainEvent;
import cc.backend.kafka.event.common.enums.DomainEventType;


public record CommentEvent (
     Long boardId,
     Long boardWriterId, //게시물 작성자 id
     Long commentId,
     Long commentWriterId //댓글 작성자 id
)  implements DomainEvent {

    @Override
    public DomainEventType getEventType() {
        return DomainEventType.COMMENT_ON_BOARD;
    }

}
