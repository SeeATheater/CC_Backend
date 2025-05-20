package cc.backend.amateurShow.controller;

import cc.backend.amateurShow.dto.AmateurEnrollRequestDTO;
import cc.backend.amateurShow.dto.AmateurEnrollResponseDTO;
import cc.backend.amateurShow.dto.AmateurShowResponseDTO;
import cc.backend.amateurShow.service.amateurShowService.AmateurService;
import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.member.MemberService;
import cc.backend.member.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@Tag(name = "소극장 공연")
@RestController
@RequiredArgsConstructor
@RequestMapping("/amateurs")
public class AmateurController {

    private final MemberService memberService;
    private final AmateurService amateurService;

    @PostMapping(value = "/enroll/{memberId}")
    @Operation(summary = "소극장 공연 생성 API")
    public ApiResponse<AmateurEnrollResponseDTO.AmateurEnrollResult> enrollShow(@PathVariable("memberId") Long memberId, @RequestBody AmateurEnrollRequestDTO requestDTO)
    {
        //@RequestPart(name = "summaryImage", required = false) MultipartFile summaryImage){
        //Member member = memberService.getMemberByToken(authorizationHeader);

        //Member member = memberService.getMemberById(memberId); // 임시용

        return ApiResponse.onSuccess(amateurService.enrollShow(memberId, requestDTO));

    }

    @GetMapping("/{amateurShowId}")
    @Operation(summary = "소극장 공연 조회 - 단건")
    public ApiResponse<AmateurShowResponseDTO.AmateurShowResult> getAmateurShow(@PathVariable Long amateurShowId){
        //Member member = memberService.getMemberByToken(authorizationHeader);
        return ApiResponse.onSuccess(amateurService.getAmateurShow(amateurShowId));
    }
}