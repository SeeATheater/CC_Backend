package cc.backend.board.dto.response;

import cc.backend.board.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Schema(description = "댓글 목록 조회 응답 DTO (트리 구조)")
public class CommentResponse {
    @Schema(description = "댓글 ID", example = "456")
    private Long commentId;

    @Schema(description = "게시글 ID", example = "123")
    private Long boardId;

    @Schema(description = "댓글 내용 (삭제된 경우 '삭제된 댓글입니다.' 표시)", example = "정말 좋은 글이네요! 감사합니다.")
    private String content;

    @Schema(description = "작성자명 (게시글 작성자: '작성자', 그 외: '익명')", example = "익명")
    private String writer; // 실명 or 익명

    @Schema(description = "삭제 여부", example = "false")
    private boolean deleted;

    @Schema(description = "부모 댓글 ID (대댓글인 경우에만 값 존재)", example = "123", nullable = true)
    private Long parentId;

    @Schema(description = "대댓글 목록 (해당 댓글의 하위 댓글들)")
    private List<CommentResponse> children;

    @Schema(description = "좋아요 수", example = "5")
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
