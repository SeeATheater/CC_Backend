package cc.backend.event.entity;

import lombok.Getter;

@Getter
public class CommentEvent {
    private final Long id;
    private final Long memberId;

    public CommentEvent(Long boardId, Long memberId) {
        this.id = boardId;
        this.memberId = memberId;
    }

}
