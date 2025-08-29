package cc.backend.admin.amateurShow.controller;

import cc.backend.admin.amateurShow.SearchField;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowListResponseDTO;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowRejectRequestDTO;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowReviseRequestDTO;
import cc.backend.admin.amateurShow.dto.AdminAmateurShowSummaryResponseDTO;
import cc.backend.admin.amateurShow.service.AdminAmateurShowService;
import cc.backend.apiPayLoad.ApiResponse;
import com.google.protobuf.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/amateurShow")
@RequiredArgsConstructor
@Tag(name = "관리자 등록 요청 관리")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAmateurShowController {

    private final AdminAmateurShowService adminAmateurShowService;


//    @GetMapping("/showList")
//    @Operation(
//            summary = "소극장 공연 관리 첫 페이지",
//            description = "공연명/등록자명/날짜별 필터를 통해 등록된 소극장 공연 리스트 조회합니다.",
//            responses = {
//                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                            responseCode = "200",
//                            description = "조회 성공",
//                            content = @Content(schema = @Schema(implementation = AdminAmateurShowListResponseDTO.class))
//                    )
//            }
//    )
//    public ApiResponse<List<AdminAmateurShowListResponseDTO>> showList(
//            @Parameter(description = "검색 조건 : 소극장 공연 명 / 등록자명 / 날짜시간",
//                    example = "SHOW_NAME")
//            @RequestParam(defaultValue = "SHOW_NAME") SearchField searchField,
//
//            @Parameter(description = "검색 키워드",
//                    example = "실종")
//            @RequestParam(required = false) String keyword,
//
//            @Parameter(description = "페이지 번호(0부터)", example = "0")
//            @RequestParam(defaultValue = "0") int page,
//
//            @Parameter(description = "페이지 크기", example = "20")
//            @RequestParam(defaultValue = "20") int size
//    ) {
//        return adminAmateurShowService.getShowList(searchField, keyword, page, size);
//    }

    @GetMapping("/{showId}")
    @Operation(
            summary = "소극장 공연 관리 - 상세",
            description = "공연명, 등록자명/아이디, 날짜·시간, 해시태그, 줄거리, 계좌번호, 연락처, 상태를 반환합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = AdminAmateurShowSummaryResponseDTO.class))
                    )
            }
    )
    public ApiResponse<AdminAmateurShowSummaryResponseDTO> getShowSummary(
            @Parameter(description = "공연 ID", example = "1")
            @PathVariable Long showId
    ) {
        return ApiResponse.onSuccess(adminAmateurShowService.getShowSummary(showId));
    }


    @PatchMapping("/{showId}/revise")
    @Operation(
            summary = "소극장 공연 관리 - 상세 - 수정하기",
            description = "관리자가 소극장 공연을 수정하는 api",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = AdminAmateurShowSummaryResponseDTO.class))
                    )
            }
    )
    public ApiResponse<AdminAmateurShowSummaryResponseDTO> reviseShow(
            @Parameter(description = "공연 ID", example = "1")
            @PathVariable Long showId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 해시태그, 줄거리, 계좌번호, 연락처 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AdminAmateurShowReviseRequestDTO.class),
                            examples = @ExampleObject(
                                    name = "소극장 수정 예시",
                                    value = """
                {
                    "hashtag": "#연극 #로맨스",
                    "summary": "겨울에 만난 두 남녀의 이야기",
                    "account": "토스 0001-0001-0001-0001",
                    "contact": "인스타그램 @example"
                }
                """
                            )
                    )
            )
            @RequestBody AdminAmateurShowReviseRequestDTO dto

    ){
        return ApiResponse.onSuccess(adminAmateurShowService.reviseShow(showId, dto));
    }

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
