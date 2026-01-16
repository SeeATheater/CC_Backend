package cc.backend.notice.event;


public record TicketReservationCommitEvent(
        Long amateurShowId,
        Long realTicketId,
        Long memberId
) { }
