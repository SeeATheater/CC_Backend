package cc.backend.event.entity;

public class PostEvent {
    private final Long boardId;
    private final Long userId;

    public PostEvent(Long boardId, Long userId) {
        this.boardId = boardId;
        this.userId = userId;
    }

}
