package cc.backend.notice.event.entity;

import lombok.Getter;

@Getter
public class CommentEvent {
    private final Long boardId;
    private final Long boardWriterId; //게시물 작성자 id
    private final Long commentId;
    private final Long commentWriterId;  //댓글 작성자 id

    public CommentEvent(Long boardId, Long boardWriterId, Long commentId, Long commentWriterId) {
        this.boardId = boardId;
        this.boardWriterId = boardWriterId;
        this.commentId = commentId;
        this.commentWriterId = commentWriterId;
    }

}
