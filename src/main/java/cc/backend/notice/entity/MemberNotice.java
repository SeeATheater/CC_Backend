package cc.backend.notice.entity;

import cc.backend.common.entity.BaseEntity;
import cc.backend.member.entity.Member;
import cc.backend.notice.entity.enums.NoticeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberNotice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "bigint")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_id")
    private Member member;

    private Boolean isread = false;

    @Builder
    public MemberNotice( Notice notice, Member member) {
        this.isread = false ;
        this.notice = notice;
        this.member = member;
    }
}
