package cc.backend.amateurShow.controller;

import cc.backend.amateurShow.dto.AmateurEnrollRequestDTO;
import cc.backend.amateurShow.dto.AmateurEnrollResponseDTO;
import cc.backend.amateurShow.dto.AmateurShowResponseDTO;
import cc.backend.amateurShow.dto.AmateurUpdateRequestDTO;
import cc.backend.amateurShow.service.amateurShowService.AmateurService;
import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.member.MemberService;
import cc.backend.member.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "소극장 공연")
@RestController
@RequiredArgsConstructor
@RequestMapping("/amateurs")
public class AmateurController {

    private final MemberService memberService;
    private final AmateurService amateurService;

    @PostMapping(value = "/enroll")
    @Operation(summary = "소극장 공연 생성 API")
    public ApiResponse<AmateurEnrollResponseDTO.AmateurEnrollResult> enrollShow(@AuthenticationPrincipal(expression = "member") Member member, @RequestBody AmateurEnrollRequestDTO requestDTO)
    {
        //@RequestPart(name = "summaryImage", required = false) MultipartFile summaryImage){
        //Member member = memberService.getMemberByToken(authorizationHeader);

        //Member member = memberService.getMemberById(memberId); // 임시용

        return ApiResponse.onSuccess(amateurService.enrollShow(member.getId(), requestDTO));

    }

    @PatchMapping("/{amateurShowId}")
    @Operation(summary = "소극장 공연 수정 API")
    public ApiResponse<AmateurEnrollResponseDTO.AmateurEnrollResult> updateShow(@PathVariable Long amateurShowId, @RequestBody AmateurUpdateRequestDTO requestDTO) {

        return ApiResponse.onSuccess(amateurService.updateShow(amateurShowId, requestDTO));
    }

    @DeleteMapping("/{amateurShowId}")
    @Operation(summary = "소극장 공연 삭제 API")
    public ApiResponse<String> deleteShow(@PathVariable Long amateurShowId) {
        amateurService.deleteShow(amateurShowId);
        return ApiResponse.onSuccess("삭제가 완료되었습니다.");
    }

    @GetMapping("/{amateurShowId}")
    @Operation(summary = "소극장 공연 조회 - 단건")
    public ApiResponse<AmateurShowResponseDTO.AmateurShowResult> getAmateurShow(@PathVariable Long amateurShowId){
        //Member member = memberService.getMemberByToken(authorizationHeader);
        return ApiResponse.onSuccess(amateurService.getAmateurShow(amateurShowId));
    }
}