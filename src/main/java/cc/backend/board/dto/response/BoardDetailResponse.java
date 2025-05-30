package cc.backend.board.dto.response;

import cc.backend.board.entity.Board;
import cc.backend.board.entity.enums.BoardType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BoardDetailResponse {
    private Long boardId;
    private BoardType boardType;
    private String title;
    private String content;
    private List<String> imgUrls;
    private int likeCount;
    private int commentCount;
    private Long memberId;
    private String writer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BoardDetailResponse from(Board board) {
        String writer;
        if (board.getBoardType() == BoardType.PROMOTION) {
            writer = board.getMember().getUsername();
        } else {
            writer = "익명";
        }
        return BoardDetailResponse.builder()
                .boardId(board.getId())
                .boardType(board.getBoardType())
                .title(board.getTitle())
                .content(board.getContent())
                .imgUrls(board.getImgUrls())
                .likeCount(board.getLikeCount())
                .commentCount(board.getCommentCount())
                .memberId(board.getMember().getId())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }
}
