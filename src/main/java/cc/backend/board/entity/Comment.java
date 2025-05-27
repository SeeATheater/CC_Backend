package cc.backend.board.entity;

import cc.backend.common.entity.BaseEntity;
import cc.backend.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    // 댓글 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 댓글이 달린 게시글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    // 부모 댓글 (null이면 일반 댓글, 있으면 대댓글)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Comment> children = new ArrayList<>();

    @Column(nullable = false)
    private int depth; //0, 1

    @Column(nullable = false)
    private boolean deleted = false;

    // 댓글 생성 메서드
    public static Comment createComment(String content, Member member, Board board) {
        return Comment.builder()
                .content(content)
                .member(member)
                .board(board)
                .depth(0)
                .build();
    }

    // 대댓글 생성 메서드
    public static Comment createReply(String content, Member member, Board board, Comment parent) {
        if (parent.getDepth() != 0) {
            throw new IllegalArgumentException("대댓글의 depth는 1까지만 허용됩니다.");
        }
        return Comment.builder()
                .content(content)
                .member(member)
                .board(board)
                .parent(parent)
                .depth(1)
                .build();
    }

    public void updateContent(String newContent) {
        if (this.deleted) {
            throw new IllegalStateException("삭제된 댓글은 수정할 수 없습니다.");
        }
        this.content = newContent;
    }

    public void softDelete() {
        this.deleted = true;
        this.content = "삭제된 댓글입니다.";
    }
}
