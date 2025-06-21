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

    private String title;

    private String content;

    @OneToMany (mappedBy = "notice", cascade = CascadeType.ALL)
    private List<MemberNotice> memberNotices = new ArrayList<>();

    @Builder
    public Notice(NoticeType type, String title, String content, List<MemberNotice> memberNotices) {
        this.type = type;
        this.title = title ;
        this.content = content;
    }
}
