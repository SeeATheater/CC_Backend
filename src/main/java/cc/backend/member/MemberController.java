package cc.backend.member;


import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.member.dto.MyPageResponseDTO;
import cc.backend.member.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{memberId}")
    @Operation(summary = "[예매자 -> 본인] 마이 페이지 조회 api", description = "마이 페이지 조회 합니다")
    public ApiResponse<MyPageResponseDTO> myPage(@AuthenticationPrincipal(expression = "member") Member member) {
        MyPageResponseDTO myPageResponseDTO = memberService.getMyPage(member.getId());

        return ApiResponse.onSuccess(myPageResponseDTO);

    }

    @PatchMapping("/{memberId}/deActive")
    @Operation(summary = "회원 탈퇴(비활성화) api", description = "회원 비활성화 하는 기능입니다.")
    public ApiResponse<MyPageResponseDTO> deactivateMember(@AuthenticationPrincipal(expression = "member") Member member) {
        MyPageResponseDTO myPageResponseDTO = memberService.deactivateMember(member.getId());
        return ApiResponse.onSuccess(myPageResponseDTO);
    }

    @PatchMapping("/{memberId}/reActive")
    @Operation(summary = "회원 활성화 api", description = "회원 활성화 하는 기능입니다.")
    public ApiResponse<MyPageResponseDTO> reactivateMember(@AuthenticationPrincipal(expression = "member") Member member) {
        MyPageResponseDTO myPageResponseDTO = memberService.reactivateMember(member.getId());
        return ApiResponse.onSuccess(myPageResponseDTO);
    }

//    @GetMapping("/{memberId}/performer")
//    @Operation(summary = "[예매자 -> 공연진] 마이 페이지 조회 api", description = "공연진 페이지 조회 합니다")
//    public ApiResponse<MyPageResponseDTO> performer(@PathVariable("memberId") Long memberId) {}
//


}
