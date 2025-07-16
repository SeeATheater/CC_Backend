package cc.backend.board.dto.response;

import cc.backend.board.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "댓글 작성/수정 응답 DTO")
public class CommentCreateResponse {

    @Schema(description = "댓글 ID", example = "456")
    private Long commentId;

    @Schema(description = "게시글 ID", example = "123")
    private Long boardId;

    @Schema(description = "댓글 내용", example = "정말 좋은 글이네요! 감사합니다.")
    private String content;

    @Schema(description = "작성자명 (게시글 작성자: '작성자', 그 외: '익명')", example = "작성자")
    private String writer;

    @Schema(description = "부모 댓글 ID (대댓글인 경우에만 값 존재)", example = "123", nullable = true)
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
