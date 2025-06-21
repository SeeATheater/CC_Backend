package cc.backend.board.entity;

import cc.backend.board.entity.enums.ReportReason;
import cc.backend.board.entity.enums.ReportTarget;
import cc.backend.common.entity.BaseEntity;
import cc.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"reporter_id", "targetType", "targetId"})
)
//UniqueConstraint : 동일 사용자가 여러번 신고하는 것 방지
public class Report extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    // 신고 대상 타입 (게시글 or 댓글)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTarget targetType;

    // 신고 대상 ID (게시글ID or 댓글ID)
    @Column(nullable = false)
    private Long targetId;

    // 신고 사유
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

}