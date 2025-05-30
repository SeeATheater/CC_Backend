package cc.backend.board.dto.request;

import lombok.Getter;

@Getter
public class CommentRequest {
    private Long memberId;
    private String content;
    private Long parentCommentId;
}

