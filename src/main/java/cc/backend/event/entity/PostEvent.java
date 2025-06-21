package cc.backend.event.entity;

import cc.backend.member.entity.Member;
import lombok.Getter;

import java.util.List;

@Getter
public class PostEvent {
    private final Long boardId;
    private final Long writerId;
    private final List<Member> members; //게시글 작성자를 좋아요한 member 리스트(memberNotice 생성시 필요 - 알림 전달용)

    public PostEvent(Long boardId, Long writerId, List<Member> members) {
        this.boardId = boardId;
        this.writerId = writerId;
        this.members = members;
    }

}
