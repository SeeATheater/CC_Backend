package cc.backend.admin.dashboard.controller;

import cc.backend.admin.dashboard.dto.ApprovalSummaryResponseDTO;
import cc.backend.admin.dashboard.dto.VisitResponseDTO;
import cc.backend.admin.dashboard.service.DashboardService;
import cc.backend.apiPayLoad.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "관리자 대쉬보드")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {
    private final DashboardService dashBoardService;

    @GetMapping("/visits/hourly")
    @Operation(summary = "시간별 방문자 수 조회")
    public ApiResponse<List<VisitResponseDTO.HourlyVisitorDTO>> getHourlyVisits() {
        return ApiResponse.onSuccess(dashBoardService.getHourlyVisits());
    }

    @GetMapping("/visits/monthly")
    @Operation(summary = "월별 방문자 수 조회")
    public ApiResponse<List<VisitResponseDTO.MonthlyVisitorDTO>> getMonthlyVisits() {
        return ApiResponse.onSuccess(dashBoardService.getMonthlyVisits());
    }

    @GetMapping("/approval")
    @Operation(summary = "등록 요청 - 간편보기")
    public ApiResponse<Slice<ApprovalSummaryResponseDTO>> getApprovalSummary(
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    ){
        return ApiResponse.onSuccess(dashBoardService.getApprovalList(page, size));
    }

}
