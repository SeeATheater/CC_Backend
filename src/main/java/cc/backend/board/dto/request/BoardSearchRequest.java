package cc.backend.board.dto.request;

import cc.backend.board.entity.enums.BoardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
public class BoardSearchRequest {
    private String keyword;
    private BoardType boardType;
    private int page = 0;
    private int size = 20;
}