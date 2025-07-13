package cc.backend.board.dto.response;

import cc.backend.board.entity.enums.BoardType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@Getter
@Schema(description = "게시글 작성/수정 응답 DTO")
public class BoardResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long boardId;

    @Schema(description = "게시판 타입", example = "NORMAL", allowableValues = {"NORMAL", "PROMOTION"})
    private BoardType boardType;

    @Schema(description = "게시글 제목", example = "안녕하세요 첫 게시글입니다")
    private String title;

    @Schema(description = "게시글 내용", example = "게시글 내용입니다. 반갑습니다!")
    private String content;

    @Schema(description = "첨부 이미지 URL 목록")
    private List<String> imgUrls;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-15T14:20:00")
    private LocalDateTime updatedAt;
}
