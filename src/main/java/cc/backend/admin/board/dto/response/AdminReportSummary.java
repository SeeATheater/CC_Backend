package cc.backend.admin.board.dto.response;

import cc.backend.board.entity.enums.ReportTarget;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminReportSummary {
    private ReportTarget targetType;
    private Long targetId; //게시글 또는 댓글 Id
    private String targetContent; // 게시글 제목 or 댓글 내용
    private Long writerId; // 작성자
    private long reportCount;
    private boolean deleted;
    private List<ReportDetail> reports;

    @Getter
    @Builder
    public static class ReportDetail {
        private Long reportId;
        private Long reporterId; //신고자
        private String reason;
        private LocalDateTime reportedAt;
    }
}
