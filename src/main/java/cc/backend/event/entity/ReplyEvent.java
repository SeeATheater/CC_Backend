package cc.backend.event.entity;

import lombok.Getter;

@Getter
public class ReplyEvent {
    private final Long newCommentId;
    private final Long parentCommentId;
    private final Long replyWriterId; //대댓글 작성자 id

    public ReplyEvent(Long newCommentId, Long parentCommentId, Long replyWriterId) {
        this.newCommentId = newCommentId;
        this.parentCommentId = parentCommentId;
        this.replyWriterId = replyWriterId;
    }
}
