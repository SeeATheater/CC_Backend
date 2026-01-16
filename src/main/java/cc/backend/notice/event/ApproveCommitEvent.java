package cc.backend.notice.event;

public record ApproveCommitEvent (
        Long amateurShowId,
        Long performerId
){}
