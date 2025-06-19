package cc.backend.board.controller;

import cc.backend.board.dto.response.ReportResponse;
import cc.backend.board.entity.enums.ReportReason;
import cc.backend.board.entity.enums.ReportTarget;
import cc.backend.board.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
@Tag(name = "신고 통합 API")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "게시글/댓글 신고", description = "게시글 또는 댓글을 신고합니다.")
    @PostMapping
    public ResponseEntity<ReportResponse> report(
            @RequestParam Long reporterId,
            @RequestParam ReportTarget targetType,
            @RequestParam Long targetId,
            @RequestParam ReportReason reason) {
        ReportResponse response=reportService.report(reporterId, targetType, targetId, reason);
        return ResponseEntity.ok(response);
    }
}
