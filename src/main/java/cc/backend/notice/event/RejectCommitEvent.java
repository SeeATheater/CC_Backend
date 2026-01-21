package cc.backend.notice.event;

public record RejectCommitEvent (
        Long amateurShowId,
        Long performerId,
        String rejectReason
){ }
