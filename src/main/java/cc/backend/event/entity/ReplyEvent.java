package cc.backend.event.entity;

import lombok.Getter;

@Getter
public class ReplyEvent {
    private final Long commentId;
    private final Long writerId; //댓글 작성자 id

    public ReplyEvent(Long commentId, Long writerId) {
        this.commentId = commentId;
        this.writerId = writerId;
    }
}
