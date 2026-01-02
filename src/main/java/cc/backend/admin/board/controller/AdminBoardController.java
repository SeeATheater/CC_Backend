package cc.backend.admin.board.controller;

import cc.backend.admin.board.dto.response.AdminBoardDetailWithCommentsResponse;
import cc.backend.admin.board.dto.response.AdminBoardListResponse;
import cc.backend.admin.board.dto.response.AdminReportSummary;
import cc.backend.admin.board.service.AdminBoardService;
import cc.backend.admin.board.service.AdminReportService;
import cc.backend.apiPayLoad.SliceResponse;
import cc.backend.board.entity.enums.ReportTarget;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/boards")
@RequiredArgsConstructor
@Tag(name = "관리자 게시글 통합 관리")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBoardController {

    private final AdminReportService adminReportService;
    private final AdminBoardService adminBoardService;

    @Operation(
            summary = "관리자 신고된 게시글/댓글 목록 조회",
            description = "신고된 모든 게시글과 댓글을 통합 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "신고 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = AdminReportSummary.class))
                    )
            })
    @GetMapping("/reports")
    public ResponseEntity<List<AdminReportSummary>> getAllReports() {
        return ResponseEntity.ok(adminReportService.getReports());
    }

    @Operation(
            summary = "관리자 게시글/댓글 삭제",
            description = "관리자가 게시글 또는 댓글을 삭제(soft delete) 처리합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "삭제 성공"
                    )
            }
    )
    @DeleteMapping
    public ResponseEntity<Void> deleteTarget(
            @Parameter(
                    description = "삭제 대상 타입 (BOARD: 게시글, COMMENT: 댓글)",
                    schema = @Schema(implementation = ReportTarget.class),
                    example = "BOARD",
                    required = true
            )
            @RequestParam ReportTarget targetType,
            @Parameter(description = "삭제 대상 ID(게시글ID 또는 댓글ID)", example = "123", required = true)
            @RequestParam Long targetId
    ) {
        adminBoardService.deleteReportedTarget(targetType, targetId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "관리자 게시글 목록 조회",
            description = "삭제된 게시글을 포함하여 모든 게시글을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "게시글 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = AdminBoardListResponse.class))
                    )
            })
    @GetMapping
    public cc.backend.apiPayLoad.ApiResponse<SliceResponse<AdminBoardListResponse>> getAllBoardsForAdmin(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "검색 키워드, 게시글 제목", example = "예매 방법 꿀팁")
            @RequestParam(required = false) String keyword) {

        Slice<AdminBoardListResponse> slice = adminBoardService.getAllBoardsForAdmin(page, size, keyword);
        return cc.backend.apiPayLoad.ApiResponse.onSuccess(SliceResponse.of(slice));
    }

    @Operation(
            summary = "관리자 게시글 상세 조회",
            description = "삭제된 게시글도 포함하여 게시글과 댓글을 상세 조회합니다. 삭제된 게시글의 경우 특별 메시지를 포함합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "게시글 상세 조회 성공",
                            content = @Content(schema = @Schema(implementation = AdminBoardDetailWithCommentsResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "게시글을 찾을 수 없음"
                    )
            })
    @GetMapping("/{boardId}")
    public ResponseEntity<AdminBoardDetailWithCommentsResponse> getBoardDetailForAdmin(
            @Parameter(description = "게시글 ID", example = "123", required = true)
            @PathVariable Long boardId) {

        AdminBoardDetailWithCommentsResponse boardDetail = adminBoardService.getBoardDetailForAdmin(boardId);
        return ResponseEntity.ok(boardDetail);
    }
}
