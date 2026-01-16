package cc.backend.notice.entity;

import cc.backend.domain.common.BaseEntity;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String personalMsg;

    private boolean isRead = false;

    @Builder
    public MemberNotice(Notice notice, Member member, String personalMsg, boolean isRead) {
        this.notice = notice;
        this.member = member;
        this.personalMsg = personalMsg;
        this.isRead = isRead;
    }

    public void updateIsRead(){
        this.isRead = true ;
    }


}
