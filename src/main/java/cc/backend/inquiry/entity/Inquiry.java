package cc.backend.inquiry.entity;

import cc.backend.domain.common.BaseEntity;
import cc.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Inquiry extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
     private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryStatus inquiryStatus;

    private String reply;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public void updateReply(String reply) {
        this.reply = reply;
        this.repliedAt = LocalDateTime.now();
        this.inquiryStatus = InquiryStatus.REPLIED;
    }
}
