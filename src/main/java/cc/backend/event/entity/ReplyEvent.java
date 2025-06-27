package cc.backend.event.entity;

import lombok.Getter;

@Getter
public class ReplyEvent {
    private final Long commentId;
    private final Long writerId; //댓글 작성자 id
    private final Long replyId; // 대댓글 commentId

    public ReplyEvent(Long commentId, Long writerId, Long replyId) {
        this.commentId = commentId;
        this.writerId = writerId;
        this.replyId = replyId;
    }
}
