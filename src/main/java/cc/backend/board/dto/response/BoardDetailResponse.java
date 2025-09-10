package cc.backend.board.dto.response;

import cc.backend.board.entity.Board;
import cc.backend.board.entity.enums.BoardType;
import cc.backend.image.entity.Image;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@Schema(description = "게시글 상세 응답 DTO")
public class BoardDetailResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long boardId;

    @Schema(description = "게시판 타입", example = "NORMAL", allowableValues = {"NORMAL", "PROMOTION"})
    private BoardType boardType;

    @Schema(description = "게시글 제목", example = "안녕하세요 첫 게시글입니다")
    private String title;

    @Schema(description = "게시글 내용", example = "게시글 내용입니다. 반갑습니다!")
    private String content;

    @Schema(description = "첨부 이미지 URL 목록")
    private List<String> imgUrls;

    @Schema(description = "좋아요 수", example = "5")
    private int likeCount;

    @Schema(description = "댓글 수", example = "3")
    private int commentCount;

    @Schema(description = "작성자 ID", example = "123")
    private Long memberId;

    @Schema(description = "작성자명 (홍보게시판: 실명, 일반게시판: 익명)", example = "홍길동")
    private String writer;

    @Schema(description = "현재 사용자가 좋아요 눌렀는지 여부", example = "true")
    private boolean liked;

    @Schema(description = "생성일시", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-15T14:20:00")
    private LocalDateTime updatedAt;

    public static BoardDetailResponse from(Board board, boolean liked, List<String> imgUrls) {
        String writer;
        if (board.getBoardType() == BoardType.PROMOTION) {
            writer = board.getMember().getUsername();
        } else {
            writer = "익명";
        }

//        // 연관관계(이미지 엔티티)에서 이미지 URL 조회 - Image 매핑없이 contentId와 filePath 로 접근
//        List<String> imgUrls = board.getImages().stream()
//                .map(Image::getImageUrl)
//                .collect(Collectors.toList());

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
                .liked(liked)
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .build();
    }
}
