package cc.backend.board.dto.request;

import cc.backend.board.entity.enums.BoardType;
import cc.backend.image.DTO.ImageRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "게시글 작성/수정 요청 DTO")
public class BoardRequest {
    @Schema(description = "게시글 제목", example = "안녕하세요 첫 게시글입니다", required = true)
    private String title;

    @Schema(description = "게시글 내용", example = "게시글 내용입니다. 반갑습니다!", required = true)
    private String content;

    @Schema(description = "게시판 타입", example = "NORMAL", allowableValues = {"NORMAL", "PROMOTION"}, required = true)
    private BoardType boardType;

    @Schema(description = "첨부 이미지 목록", nullable = true)
    private List<ImageRequestDTO.PartialImageRequestDTO> imageRequestDTOs;
}
