package cc.backend.board.dto.response;

import cc.backend.board.entity.enums.ReportReason;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardReportResponse {
    private Long boardId;
    private Long memberId;
    private ReportReason reason;
    private String reasonDescription;
}