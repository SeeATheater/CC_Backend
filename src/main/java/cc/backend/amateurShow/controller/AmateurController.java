package cc.backend.amateurShow.controller;

import cc.backend.amateurShow.dto.AmateurEnrollRequestDTO;
import cc.backend.amateurShow.dto.AmateurEnrollResponseDTO;
import cc.backend.amateurShow.service.amateurShowService.AmateurService;
import cc.backend.apiPayLoad.ApiResponse;
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

    @PostMapping(value = "/enroll")
    @Operation(summary = "소극장 공연 생성 API")
    public ApiResponse<AmateurEnrollResponseDTO.AmateurEnrollResult> enrollShow(@RequestHeader("Authorization") String authorizationHeader, @RequestBody AmateurEnrollRequestDTO requestDTO)
    {
        //@RequestPart(name = "summaryImage", required = false) MultipartFile summaryImage){
        Member member = memberService.getMemberByToken(authorizationHeader);

        return ApiResponse.onSuccess(amateurService.enrollShow(member, requestDTO));

    }
}