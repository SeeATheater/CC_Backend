package cc.backend.board.entity;

import cc.backend.common.entity.BaseEntity;
import cc.backend.board.entity.enums.BoardType;
import cc.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE board SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoardType boardType; //NORMAL, PROMOTION, HOT

    @ElementCollection
    private List<String> imgUrls;

    private int likeCount;
    private int commentCount;
    private int commentMaxIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;

    // ----- method -----
    public void update(String title, String content, List<String> imgUrls, BoardType boardType) {
        this.title = title;
        this.content = content;
        this.imgUrls = imgUrls;
        this.boardType = boardType;
    }

    // 좋아요 카운트 증가
    public void increaseLikeCount() {
        this.likeCount++;
    }

    // 좋아요 카운트 감소
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
