package cc.backend.admin.board.controller;

import cc.backend.admin.board.dto.response.AdminReportSummary;
import cc.backend.admin.board.service.AdminReportService;
import cc.backend.board.entity.enums.ReportTarget;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Tag(name = "관리자 신고 통합 관리")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @Operation(
            summary = "신고된 게시글/댓글 목록 조회",
            description = "신고된 모든 게시글과 댓글을 통합 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "신고 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = AdminReportSummary.class))
                    )
            })
    @GetMapping
    public ResponseEntity<List<AdminReportSummary>> getAllReports() {
        return ResponseEntity.ok(adminReportService.getReports());
    }

    @Operation(
            summary = "신고된 게시글/댓글 삭제",
            description = "관리자가 신고된 게시글 또는 댓글을 삭제(soft delete) 처리합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "삭제 성공"
                    )
            }
    )
    @DeleteMapping
    public ResponseEntity<Void> deleteReportedTarget(
            @Parameter(
                    description = "신고 대상 타입 (BOARD: 게시글, COMMENT: 댓글)",
                    schema = @Schema(implementation = ReportTarget.class),
                    example = "BOARD",
                    required = true
            )
            @RequestParam ReportTarget targetType,
            @Parameter(description = "신고 대상 ID(게시글ID 또는 댓글ID)", example = "123", required = true)
            @RequestParam Long targetId
    ) {
        adminReportService.deleteReportedTarget(targetType, targetId);
        return ResponseEntity.ok().build();
    }
}
