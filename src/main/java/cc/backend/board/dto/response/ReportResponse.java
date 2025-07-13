package cc.backend.board.dto.response;

import cc.backend.board.entity.enums.ReportReason;
import cc.backend.board.entity.enums.ReportTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "신고 요청 DTO")
public class ReportResponse {
    @Schema(description = "신고 대상 타입", example = "BOARD", allowableValues = {"BOARD", "COMMENT"}, required = true)
    private ReportTarget targetType;

    @Schema(description = "신고 대상 ID (게시글 ID 또는 댓글 ID)", example = "123", required = true)
    private Long targetId; //게시글 또는 댓글

    @Schema(description = "신고자 ID", example = "456")
    private Long reporterId; //신고자

    @Schema(description = "신고 사유", example = "INAPPROPRIATE",
            allowableValues = {"INAPPROPRIATE", "ABUSE", "FRAUD", "OBSCENE"}, required = true)
    private ReportReason reason;

    @Schema(description = "신고 사유 설명")
    private String reasonDescription;
}