package cc.backend.event.entity;

import lombok.Getter;

@Getter
public class CommentEvent {
    private final Long boardId;
    private final Long writerId; //게시물 작성자 id

    public CommentEvent(Long boardId, Long writerId) {
        this.boardId = boardId;
        this.writerId = writerId;
    }

}
