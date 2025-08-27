package cc.backend.admin.board.dto.response;

import cc.backend.board.entity.Board;
import cc.backend.board.entity.enums.BoardType;
import cc.backend.image.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class AdminBoardDetailResponse {

    private Long boardId;
    private String title;
    private String content;
    private BoardType boardType;
    private Long authorId;
    private String authorNickname;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private List<String> imgUrls;
    private String specialMessage; // soft delete 메시지용

    public static AdminBoardDetailResponse from(Board board) {
        List<String> imageUrls = board.getImages() != null ?
                board.getImages().stream()
                        .map(Image::getImageUrl)
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        return AdminBoardDetailResponse.builder()
                .boardId(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .boardType(board.getBoardType())
                .authorId(board.getMember().getId())
                .authorNickname(board.getMember().getUsername())
                .likeCount(board.getLikeCount())
                .commentCount(board.getCommentCount())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .isDeleted(board.isDeleted())
                .imgUrls(imageUrls)
                .specialMessage(board.isDeleted() ?
                        "soft delete 게시물입니다. 자세한건 DB를 참고해주세요" : null)
                .build();
    }
}
