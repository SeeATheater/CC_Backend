package cc.backend.event.entity;

import lombok.Getter;

@Getter
public class ReplyEvent {
    private final Long commentId;
    private final Long commentWriterId; //댓글 작성자 id
    private final Long replyId; // 대댓글 commentId
    private final Long replyWriterId; //대댓글 작성자 id

    public ReplyEvent(Long commentId, Long commentWriterId, Long replyId, Long replyWriterId) {
        this.commentId = commentId;
        this.commentWriterId = commentWriterId;
        this.replyId = replyId;
        this.replyWriterId = replyWriterId;
    }
}
