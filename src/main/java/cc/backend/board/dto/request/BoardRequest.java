package cc.backend.board.dto.request;

import cc.backend.board.entity.enums.BoardType;
import cc.backend.image.DTO.ImageRequestDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class BoardRequest {
    private String title;
    private String content;
    private BoardType boardType;
    private List<ImageRequestDTO.PartialImageRequestDTO> imageRequestDTOs;
}
