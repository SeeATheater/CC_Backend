package cc.backend.event.entity;

import cc.backend.member.entity.Member;
import lombok.Getter;

import java.util.List;

@Getter
public class NewShowEvent {
    private final Long amateurShowId;
    private final Long memberId;    //등록자 id
    private final List<Member> members; // 등록자를 좋아요한 member 리스트(memberNotice 생성시 필요 - 알림 전달용)

    public NewShowEvent(Long amateurShowId, Long memberId, List<Member> members) {
        this.amateurShowId = amateurShowId;
        this.memberId = memberId;
        this.members = members;
    }

}
