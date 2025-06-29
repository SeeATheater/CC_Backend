package cc.backend.board.dto.response;

import cc.backend.board.entity.Board;
import cc.backend.board.entity.enums.BoardType;
import cc.backend.image.entity.Image;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

        // 연관관계(이미지 엔티티)에서 이미지 URL 조회
        List<String> imgUrls = board.getImages().stream()
                .map(Image::getImageUrl)
                .collect(Collectors.toList());

        return BoardDetailResponse.builder()
                .boardId(board.getId())
                .boardType(board.getBoardType())
                .title(board.getTitle())
                .content(board.getContent())
                .imgUrls(imgUrls)
                .likeCount(board.getLikeCount())
                .commentCount(board.getCommentCount())
                .memberId(board.getMember().getId())
                .writer(writer)
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }
}
