package cc.backend.board.entity;

import cc.backend.board.entity.enums.BoardType;
import cc.backend.domain.common.BaseEntity;
import cc.backend.image.entity.Image;
import cc.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
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
    private BoardType boardType; //NORMAL, PROMOTION

    private int likeCount;

    private int commentCount = 0;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(fetch = FetchType.LAZY)
    @Where(clause = "file_path = 'board'")
    private List<Image> images = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;



    // ----- method -----
    public void update(String title, String content, BoardType boardType) {
        this.title = title;
        this.content = content;
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

    public void increaseCommentCount() {
        this.commentCount++;
    }
    public void decreaseCommentCount() {
        if (this.commentCount > 0) this.commentCount--;
    }
}
