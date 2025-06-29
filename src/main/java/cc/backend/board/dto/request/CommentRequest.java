package cc.backend.board.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "댓글 요청", example = """
{
  "content": "이것은 댓글 내용입니다",
  "parentCommentId": null
}
""")
public class CommentRequest {
    private String content;
    private Long parentCommentId;
}

