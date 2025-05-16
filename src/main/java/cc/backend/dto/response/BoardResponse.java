package cc.backend.dto.response;

import cc.backend.entity.enums.BoardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

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
}
