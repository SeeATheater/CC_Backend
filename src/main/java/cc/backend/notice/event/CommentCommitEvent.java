package cc.backend.notice.event;

public record CommentCommitEvent (
        Long boardId,
        Long boardWriterId,
        Long commentId,
        Long commentWriterId
){}
