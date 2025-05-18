package cc.backend.board.dto.response;

import cc.backend.board.entity.enums.BoardType;
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
public class BoardResponse {
    private Long boardId;
    private BoardType boardType;
    private String title;
    private String content;
    private List<String> imgUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
