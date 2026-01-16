package cc.backend.notice.event.entity;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurTicket;
import cc.backend.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TicketReservationEvent {
    private final AmateurShow amateurShow;
    private final AmateurTicket amateurTicket;
    private final Member member;  //예약자

    public TicketReservationEvent(AmateurShow amateurShow, AmateurTicket amateurTicket, Member member) {
        this.amateurShow = amateurShow;
        this.amateurTicket = amateurTicket;
        this.member = member;
    }
}
