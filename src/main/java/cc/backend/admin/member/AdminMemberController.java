package cc.backend.admin.member;

import cc.backend.admin.member.dto.AdminMemberListResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/member")
@Tag(name  = "관리자 사용자 관리")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMemberController {
    private final AdminMemberService adminMemberService;

    @Operation(summary = "사용자 관리 리스트 조회", description = "모든 사용자를 id순으로 리스트로 조회합니다.")
    @GetMapping("/list")
    public Slice<AdminMemberListResponseDTO> getAllMember(
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size
    ){
        return adminMemberService.getMemberList(page, size);
    }
}
