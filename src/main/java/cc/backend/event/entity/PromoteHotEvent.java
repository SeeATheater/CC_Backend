package cc.backend.event.entity;

import cc.backend.member.entity.Member;
import lombok.Getter;

import java.util.List;

@Getter
public class PromoteHotEvent {
    private final Long boardId;
    private final Long writerId;

    public PromoteHotEvent(Long boardId, Long writerId) {
        this.boardId = boardId;
        this.writerId = writerId;
    }
}
