package cc.backend.admin.amateurShow.controller;

import cc.backend.admin.amateurShow.dto.AdminAmateurShowRejectRequestDTO;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowSummaryResponseDTO;
import cc.backend.admin.amateurShow.service.AdminAmateurShowService;
import cc.backend.apiPayLoad.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/approval")
@RequiredArgsConstructor
@Tag(name = "관리자 등록 요청 관리")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApprovalController {

    private final AdminAmateurShowService adminAmateurShowService;

    @PatchMapping("/{showId}/approve")
    @Operation(
            summary = "소극장 공연 관리 - 최종 등록",
            description = "심사결과를 ‘확인(승인)’으로 설정합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "승인 성공",
                            content = @Content(schema = @Schema(implementation = AdminAmateurShowSummaryResponseDTO.class))
                    )
            }
    )
    public ApiResponse<AdminAmateurShowSummaryResponseDTO> approve(
            @Parameter(description = "공연 ID", example = "1") @PathVariable Long showId
    ) {
        return ApiResponse.onSuccess(adminAmateurShowService.approveShow(showId));
    }

    @PatchMapping("/{showId}/reject")
    @Operation(
            summary = "소극장 공연 관리 - 반려",
            description = "심사결과를 ‘반려’로 설정하고 반려 사유를 남깁니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "반려 성공",
                            content = @Content(schema = @Schema(implementation = AdminAmateurShowSummaryResponseDTO.class))
                    )
            }
    )
    public ApiResponse<AdminAmateurShowSummaryResponseDTO> reject(
            @Parameter(description = "공연 ID", example = "1") @PathVariable Long showId,
            @RequestBody(required = false) AdminAmateurShowRejectRequestDTO body
    ) {
        return ApiResponse.onSuccess(adminAmateurShowService.rejectShow(showId, body));
    }
}
