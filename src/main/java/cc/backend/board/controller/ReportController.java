package cc.backend.board.controller;

import cc.backend.board.dto.response.ReportResponse;
import cc.backend.board.entity.enums.ReportReason;
import cc.backend.board.entity.enums.ReportTarget;
import cc.backend.board.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards/reports")
@Tag(name = "게시글/댓글 신고")
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "게시글/댓글 신고",
            description = "게시글 또는 댓글을 신고합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "신고 성공",
                            content = @Content(schema = @Schema(implementation = ReportResponse.class))
                    )
            }
    )
    @PostMapping
    public ResponseEntity<ReportResponse> report(
            @Parameter(description = "신고자(회원) ID", example = "1", required = true)
            @RequestParam Long reporterId,
            @Parameter(
                    description = "신고 대상 타입 (BOARD: 게시글, COMMENT: 댓글)",
                    schema = @Schema(implementation = ReportTarget.class),
                    example = "BOARD",
                    required = true
            )
            @RequestParam ReportTarget targetType,
            @Parameter(description = "신고 대상 ID(게시글ID 또는 댓글ID)", example = "123", required = true)
            @RequestParam Long targetId,
            @Parameter(
                    description = "신고 사유 (INAPPROPRIATE: 게시판 성격에 부적절함, ABUSE: 욕설/비하, FRAUD: 사칭/사기, OBSCENE: 음란물/불건전한 행위)",
                    schema = @Schema(implementation = ReportReason.class),
                    example = "ABUSE",
                    required = true
            )
            @RequestParam ReportReason reason
    ){
        ReportResponse response=reportService.report(reporterId, targetType, targetId, reason);
        return ResponseEntity.ok(response);
    }
}
