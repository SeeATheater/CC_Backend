package cc.backend.admin.member;

import cc.backend.admin.member.dto.AdminMemberDetailResponseDTO;
import cc.backend.admin.member.dto.AdminMemberListResponseDTO;
import cc.backend.admin.member.dto.UpdateMemberDetailRequestDTO;
import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.apiPayLoad.PageResponse;
import cc.backend.apiPayLoad.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/member")
@Tag(name  = "관리자 사용자 관리")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMemberController {
    private final AdminMemberService adminMemberService;

    @Operation(summary = "사용자 관리 리스트 조회", description = "모든 사용자를 id순으로 리스트로 조회합니다.")
    @GetMapping("/list")
    public ApiResponse<PageResponse<AdminMemberListResponseDTO>> getAllMember(
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "검색할 사용자 아이디(username)", example = "jihee")
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.onSuccess(adminMemberService.getMemberList(page, size, keyword));
    }

    @Operation(summary = "사용자 관리-상세", description = "모든 사용자를 id순으로 리스트로 조회합니다.")
    @GetMapping("/{memberId}")
    public ApiResponse<AdminMemberDetailResponseDTO> getMemberDetail(

            @Parameter(description = "사용자 id", example = "1")
            @PathVariable Long memberId
    ){
        return ApiResponse.onSuccess(adminMemberService.getMemberDetail(memberId));
    }

    @Operation(summary = "사용자 관리-수정하기")
    @PutMapping("/{memberId}")
    public ApiResponse<AdminMemberDetailResponseDTO> updateMemberDetail(
            @Parameter(description = "사용자 id", example = "1")
            @PathVariable Long memberId,
            @RequestBody UpdateMemberDetailRequestDTO dto
    ){
        return ApiResponse.onSuccess(adminMemberService.updateMemberDetail(memberId, dto));
    }
}
