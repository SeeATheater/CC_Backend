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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "소극장 공연")
@RestController
@RequiredArgsConstructor
@RequestMapping("/amateurs")
public class AmateurController {

    private final MemberService memberService;
    private final AmateurService amateurService;

    @PostMapping(value = "/enroll")
    @Operation(summary = "소극장 공연 생성 API")
    public ApiResponse<AmateurEnrollResponseDTO.AmateurEnrollResult> enrollShow(@AuthenticationPrincipal(expression = "member") Member member, @RequestBody AmateurEnrollRequestDTO requestDTO) {
        return ApiResponse.onSuccess(amateurService.enrollShow(member.getId(), requestDTO));
    }

    @PatchMapping("/{amateurShowId}")
    @Operation(summary = "소극장 공연 수정 API")
    public ApiResponse<AmateurEnrollResponseDTO.AmateurEnrollResult> updateShow(@AuthenticationPrincipal(expression = "member") Member member, @PathVariable Long amateurShowId, @RequestBody AmateurUpdateRequestDTO requestDTO) {
        return ApiResponse.onSuccess(amateurService.updateShow(member.getId(), amateurShowId, requestDTO));
    }

    @DeleteMapping("/{amateurShowId}")
    @Operation(summary = "소극장 공연 삭제 API")
    public ApiResponse<String> deleteShow(@AuthenticationPrincipal(expression = "member") Member member, @PathVariable Long amateurShowId) {
        amateurService.deleteShow(member.getId(), amateurShowId);
        return ApiResponse.onSuccess("삭제가 완료되었습니다.");
    }

    @GetMapping("/{amateurShowId}")
    @Operation(summary = "소극장 공연 조회 - 단건")
    public ApiResponse<AmateurShowResponseDTO.AmateurShowResult> getAmateurShow(@AuthenticationPrincipal(expression = "member") Member member,
                                                                                @PathVariable Long amateurShowId){
        return ApiResponse.onSuccess(amateurService.getAmateurShow(member.getId(), amateurShowId));
    }

    @GetMapping("/ranking")
    @Operation(summary = "소극장 공연 랭킹 조회 API")
    public ApiResponse<List<AmateurShowResponseDTO.AmateurShowList>> getShowRanking(@AuthenticationPrincipal(expression = "member") Member member) {
        return ApiResponse.onSuccess(amateurService.getShowRanking(member.getId()));
    }

    @GetMapping("/today")
    @Operation(summary = "오늘 진행하는 소극장 공연 조회 API")
    public ApiResponse<List<AmateurShowResponseDTO.AmateurShowList>> getShowToday(@AuthenticationPrincipal(expression = "member") Member member) {
        return ApiResponse.onSuccess(amateurService.getShowToday(member.getId()));
    }

    @GetMapping("/ongoing")
    @Operation(summary = "현재 진행중인 소극장 공연 조회 API")
    public ApiResponse<Page<AmateurShowResponseDTO.AmateurShowList>> getShowOngoing(@AuthenticationPrincipal(expression = "member") Member member,
                                                                                    @ParameterObject Pageable pageable) {
        return ApiResponse.onSuccess(amateurService.getShowOngoing(member.getId(), pageable));
    }

    @GetMapping("/closing")
    @Operation(summary = "오늘 마감인 공연 조회 API")
    public ApiResponse<List<AmateurShowResponseDTO.AmateurShowList>> getShowClosing(@AuthenticationPrincipal(expression = "member") Member member) {
        return ApiResponse.onSuccess(amateurService.getShowClosing(member.getId()));
    }

    @GetMapping("/incoming")
    @Operation(summary = "공연 임박인 공연 조회 API")
    public ApiResponse<List<AmateurShowResponseDTO.AmateurShowList>> getShowIncoming(@AuthenticationPrincipal(expression = "member") Member member) {
        return ApiResponse.onSuccess(amateurService.getIncomingShow(member.getId()));
    }
}