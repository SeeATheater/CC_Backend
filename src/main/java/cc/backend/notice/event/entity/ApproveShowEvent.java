package cc.backend.notice.event.entity;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.member.entity.Member;
import lombok.Getter;

@Getter
public class ApproveShowEvent {
    private final AmateurShow amateurShow;
    private final Member member;   //공연 등록자

    public ApproveShowEvent(AmateurShow amateurShow, Member member) {
        this.amateurShow = amateurShow;
        this.member = member;
    }
}
