package cc.backend.admin.board.dto.response;

import cc.backend.board.entity.enums.ReportTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
@Schema(description = "관리자용 신고 통합 요약 DTO")
public class AdminReportSummary {

    @Schema(description = "신고 대상 타입 (BOARD: 게시글, COMMENT: 댓글)", example = "BOARD")
    private ReportTarget targetType;

    @Schema(description = "신고 대상 ID (게시글 또는 댓글 ID)", example = "123")
    private Long targetId;

    @Schema(description = "신고 대상 내용 (게시글 제목 또는 댓글 내용)", example = "이 게시글은 부적절합니다.")
    private String targetContent;

    @Schema(description = "작성자 ID", example = "45")
    private Long writerId;

    @Schema(description = "신고 누적 횟수", example = "3")
    private long reportCount;

    @Schema(description = "삭제 여부 (true: 삭제됨, false: 삭제되지 않음)", example = "false")
    private boolean deleted;

    @Schema(description = "신고 상세 내역 리스트")
    private List<ReportDetail> reports;

    @Getter
    @Builder
    @Schema(description = "신고 상세 내역")
    public static class ReportDetail {

        @Schema(description = "신고 ID", example = "1001")
        private Long reportId;

        @Schema(description = "신고자 ID", example = "99")
        private Long reporterId;

        @Schema(description = "신고 사유", example = "ABUSIVE_LANGUAGE")
        private String reason;

        @Schema(description = "신고 일시", example = "2025-06-20T12:34:56")
        private LocalDateTime reportedAt;
    }
}