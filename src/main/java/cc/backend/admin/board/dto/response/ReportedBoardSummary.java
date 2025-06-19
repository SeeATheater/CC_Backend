package cc.backend.admin.board.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReportedBoardSummary {
    private Long boardId;
    private String title;
    private Long writerId;
    private long reportCount;
    private boolean deleted;
    private List<ReportDetail> reports;

    @Getter
    @Builder
    public static class ReportDetail {
        private Long reportId;
        private Long reporterId;
        private String reason;
        private LocalDateTime reportedAt;
    }
}
