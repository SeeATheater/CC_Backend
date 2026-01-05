package cc.backend.amateurShow.controller;

import cc.backend.amateurShow.dto.AmateurEnrollRequestDTO;
import cc.backend.amateurShow.dto.AmateurEnrollResponseDTO;
import cc.backend.amateurShow.dto.AmateurShowResponseDTO;
import cc.backend.amateurShow.dto.AmateurShowResponseDTO.AmateurShowList;
import cc.backend.amateurShow.dto.AmateurUpdateRequestDTO;
import cc.backend.amateurShow.service.amateurShowService.AmateurService;
import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.apiPayLoad.SliceResponse;
import cc.backend.member.MemberService;
import cc.backend.member.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "소극장 공연")
@RestController
@RequiredArgsConstructor
@RequestMapping("/amateurs")
public class AmateurController {

    private final AmateurService amateurService;

    @PreAuthorize("hasRole('PERFORMER')")
    @PostMapping(value = "/enroll")
    @Operation(summary = "소극장 공연 생성 API")
    public ApiResponse<AmateurEnrollResponseDTO.AmateurEnrollResult> enrollShow(@AuthenticationPrincipal(expression = "member") Member member, @Valid @RequestBody AmateurEnrollRequestDTO requestDTO) {
        return ApiResponse.onSuccess(amateurService.enrollShow(member.getId(), requestDTO));
    }

    @PreAuthorize("hasRole('PERFORMER')")
    @PatchMapping("/{amateurShowId}")
    @Operation(summary = "소극장 공연 수정 API")
    public ApiResponse<AmateurEnrollResponseDTO.AmateurEnrollResult> updateShow(@AuthenticationPrincipal(expression = "member") Member member, @PathVariable Long amateurShowId, @Valid @RequestBody AmateurUpdateRequestDTO requestDTO) {
        return ApiResponse.onSuccess(amateurService.updateShow(member.getId(), amateurShowId, requestDTO));
    }

    @PreAuthorize("hasRole('PERFORMER')")
    @DeleteMapping("/{amateurShowId}")
    @Operation(summary = "소극장 공연 삭제 API")
    public ApiResponse<String> deleteShow(@AuthenticationPrincipal(expression = "member") Member member, @PathVariable Long amateurShowId) {
        amateurService.deleteShow(member.getId(), amateurShowId);
        return ApiResponse.onSuccess("삭제가 완료되었습니다.");
    }

    @GetMapping("/{amateurShowId}")
    @Operation(summary = "소극장 공연 조회 - 단건")
    public ApiResponse<AmateurShowResponseDTO.AmateurShowResult> getAmateurShow(@PathVariable Long amateurShowId){
        return ApiResponse.onSuccess(amateurService.getAmateurShow(amateurShowId));
    }

    @GetMapping("/ranking")
    @Operation(summary = "소극장 공연 랭킹 조회 API")
    public ApiResponse<List<AmateurShowResponseDTO.AmateurShowList>> getShowRanking() {
        return ApiResponse.onSuccess(amateurService.getShowRanking());
    }

    @GetMapping("/today")
    @Operation(summary = "오늘 진행하는 소극장 공연 조회 API")
    public ApiResponse<SliceResponse<AmateurShowResponseDTO.AmateurShowList>> getShowToday( @ParameterObject Pageable pageable) {
        return ApiResponse.onSuccess(SliceResponse.of(amateurService.getShowToday(pageable)));
    }

    @GetMapping("/ongoing")
    @Operation(summary = "현재 진행중인 소극장 공연 조회 API")
    public ApiResponse<SliceResponse<AmateurShowList>> getShowOngoing( @ParameterObject Pageable pageable
    ) {
        Slice<AmateurShowList> sliceResult = amateurService.getShowOngoing(pageable);
        return ApiResponse.onSuccess(SliceResponse.of(sliceResult));
    }

    @GetMapping("/closing")
    @Operation(summary = "오늘 마감인 공연 조회 API")
    public ApiResponse<List<AmateurShowResponseDTO.AmateurShowList>> getShowClosing() {
        return ApiResponse.onSuccess(amateurService.getShowClosing());
    }

    @GetMapping("/recentlyHot")
    @Operation(summary = "요즘 핫한 소극장 연극 조회 API")
    public ApiResponse<List<AmateurShowResponseDTO.AmateurShowList>> getRecentlyHotShow() {
        return ApiResponse.onSuccess(amateurService.getRecentlyHotShow());
        }

    @GetMapping("/incoming")
    @Operation(summary = "임박한 공연 조회 API")
    public ApiResponse<SliceResponse<AmateurShowResponseDTO.AmateurShowList>> getShowIncoming(@ParameterObject Pageable pageable) {
        Slice<AmateurShowList> sliceResult = amateurService.getShowToday(pageable);
        return ApiResponse.onSuccess(SliceResponse.of(sliceResult));
    }
}