package cc.backend.notice.entity;

import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.domain.common.BaseEntity;
import cc.backend.member.entity.Member;
import cc.backend.notice.entity.enums.NoticeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "bigint")
    private Long id;

    @Enumerated(EnumType.STRING)
    private NoticeType type;

    private String message;

    //boardId, amateurShowId, ticketId 등 링크로 연결되는 대상 id
    private Long contentId;

    @OneToMany (mappedBy = "notice", cascade = CascadeType.ALL)
    private List<MemberNotice> memberNotices = new ArrayList<>();

    @Builder
    public Notice(NoticeType type, String message, Long contentId) {
        this.type = type;
        this.message = message ;
        this.contentId = contentId;
    }
}
