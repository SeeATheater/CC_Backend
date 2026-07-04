package cc.backend.event.entity;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.member.entity.Member;
import lombok.Getter;

@Getter
public class TicketReservationEvent {
    private final AmateurShow amateurShow;
    private final Long realTicketId;
    private final Member member;  //예약자

    public TicketReservationEvent(AmateurShow amateurShow, Long realTicketId, Member member) {
        this.amateurShow = amateurShow;
        this.realTicketId = realTicketId;
        this.member = member;
    }
}
