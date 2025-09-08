package cc.backend.admin.board.dto.response;

import cc.backend.board.dto.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AdminBoardDetailWithCommentsResponse {
    private AdminBoardDetailResponse boardDetail;
    private List<CommentResponse> comments;

    public static AdminBoardDetailWithCommentsResponse of(
            AdminBoardDetailResponse boardDetail,
            List<CommentResponse> comments) {
        return AdminBoardDetailWithCommentsResponse.builder()
                .boardDetail(boardDetail)
                .comments(comments)
                .build();
    }


}

