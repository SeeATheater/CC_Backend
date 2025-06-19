package cc.backend.board.dto.response;

import cc.backend.board.entity.enums.ReportReason;
import cc.backend.board.entity.enums.ReportTarget;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportResponse {
    private ReportTarget targetType;
    private Long targetId; //게시글 또는 댓글
    private Long reporterId; //신고자
    private ReportReason reason;
    private String reasonDescription;
}