package cc.backend.admin.board.controller;

import cc.backend.admin.board.dto.response.ReportedBoardSummary;
import cc.backend.admin.board.service.AdminBoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/boards")
@RequiredArgsConstructor
@Tag(name = "관리자 게시글 관리")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBoardController {

    private final AdminBoardService adminBoardService;

    @Operation(summary = "신고된 게시글 목록 조회", description = "신고된 모든 게시글과 신고 내역을 조회합니다.")
    @GetMapping("/reports")
    public ResponseEntity<List<ReportedBoardSummary>> getReportedBoards() {
        return ResponseEntity.ok(adminBoardService.getReportedBoards());
    }

    @Operation(summary = "신고된 게시글 삭제", description = "관리자가 신고된 게시글을 삭제 처리합니다.")
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteReportedBoard(@PathVariable Long boardId) {
        adminBoardService.adminDeleteBoard(boardId);
        return ResponseEntity.ok().build();
    }
}
