package cc.backend.board.dto.request;

import cc.backend.board.entity.enums.BoardType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "게시글 검색 요청 DTO")
public class BoardSearchRequest {
    @Schema(description = "검색 키워드", example = "안녕하세요", nullable = true)
    private String keyword;

    @Schema(description = "게시판 타입", example = "NORMAL", allowableValues = {"NORMAL", "PROMOTION"}, required = true)
    private BoardType boardType;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
    private int page = 0;

    @Schema(description = "페이지 크기", example = "20", defaultValue = "20")
    private int size = 20;
}