package cc.backend.board.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "댓글 작성/수정 요청 DTO")
public class CommentRequest {
    @Schema(description = "댓글 내용", example = "정말 좋은 글이네요! 감사합니다.", required = true)
    private String content;

    @Schema(description = "부모 댓글 ID (대댓글 작성 시에만 필요, 일반 댓글은 null)", example = "123", nullable = true)
    private Long parentCommentId;
}

