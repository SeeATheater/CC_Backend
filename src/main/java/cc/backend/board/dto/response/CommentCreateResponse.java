package cc.backend.board.dto.response;

import cc.backend.board.entity.Comment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentCreateResponse {
    private Long commentId;
    private Long boardId;
    private String content;
    private String writer;
    private Long parentId;

    public static CommentCreateResponse from(Comment comment, Long boardWriterId) {
        String writer = comment.getMember().getId().equals(boardWriterId)
                ? "작성자"
                : "익명";
        return CommentCreateResponse.builder()
                .commentId(comment.getId())
                .boardId(comment.getBoard().getId())
                .content(comment.isDeleted() ? "삭제된 댓글입니다." : comment.getContent())
                .writer(writer)
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .build();
    }
}
