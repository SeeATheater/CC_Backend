package cc.backend.admin.board.dto.response;

import cc.backend.board.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminBoardListResponse {

    private Long boardId;
    private String title;
    private Long authorId;
    private String authorNickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminBoardListResponse from(Board board) {
        return AdminBoardListResponse.builder()
                .boardId(board.getId())
                .title(board.getTitle())
                .authorId(board.getMember().getId())
                .authorNickname(board.getMember().getUsername())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }
}

