package cc.backend.board.dto.response;

import cc.backend.board.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class CommentResponse {
    private Long commentId;
    private Long boardId;
    private String content;
    private String writer; // 실명 or 익명
    private boolean deleted;
    private Long parentId;
    private List<CommentResponse> children;
    private int likeCount;

    public static CommentResponse from(Comment comment, Long boardWriterId) {
        String writer;
        if (comment.getMember().getId().equals(boardWriterId)) {
            writer = "작성자";
        } else {
            writer = "익명";
        }
        return CommentResponse.builder()
                .commentId(comment.getId())
                .boardId(comment.getBoard().getId())
                .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .writer(writer)
                .deleted(comment.isDeleted())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .children(new ArrayList<>())
                .likeCount(comment.getLikeCount())
                .build();
    }
}
