package cc.backend.notice.event;

public record ReplyCommitEvent (
        Long commentId,
        Long commentWriterId,
        Long replyId,
        Long replyWriterId
){ }
