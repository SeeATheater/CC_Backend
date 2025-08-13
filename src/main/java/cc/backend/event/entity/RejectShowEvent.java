package cc.backend.event.entity;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.member.entity.Member;
import lombok.Getter;

@Getter
public class RejectShowEvent {
    private final AmateurShow amateurShow;
    private final Member member;   //공연 등록자

    public RejectShowEvent(AmateurShow amateurShow, Member member) {
        this.amateurShow = amateurShow;
        this.member = member;
    }
}
