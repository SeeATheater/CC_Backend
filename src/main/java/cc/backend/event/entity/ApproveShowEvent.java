package cc.backend.event.entity;

import lombok.Getter;

@Getter
public class ApproveShowEvent {
    private final Long amateurShowId;
    private final Long memberId;   //공연 등록자
    private final String showName;
    private final String hashtag;

    public ApproveShowEvent(Long amateurShowId, Long memberId, String showName, String hashtag) {
        this.amateurShowId = amateurShowId;
        this.memberId = memberId;
        this.showName = showName;
        this.hashtag = hashtag;
    }
}
