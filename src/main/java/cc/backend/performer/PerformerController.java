package cc.backend.performer;

import cc.backend.amateurShow.dto.AmateurShowResponseDTO;
import cc.backend.amateurShow.entity.AmateurShowStatus;
import cc.backend.amateurShow.service.amateurShowService.AmateurService;
import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.apiPayLoad.SliceResponse;
import cc.backend.member.entity.Member;
import cc.backend.performer.dto.ShowReservationResponseDTO;
import com.google.protobuf.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공연진 마이페이지")
@RestController
@RequiredArgsConstructor
@RequestMapping("/performer-page")
public class PerformerController {

    private final AmateurService amateurService;
    private final PerformerService performerService;

    @PreAuthorize("hasRole('PERFORMER')")
    @GetMapping("/myShow")
    @Operation(summary = "공연진 내가 등록한 공연 조회", description = "등록자 계정으로 등록한 공연들을 무한 스크롤 방식으로 조회합니다.")
    public ApiResponse<SliceResponse<AmateurShowResponseDTO.MyShowAmateurShowList>> getMyShows(
            @Parameter(description = "작성자 회원 ID", required = true)
            @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(description = "페이지 번호(0부터 시작)", required = true)
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", required = true)
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "공연 상태 필터 (전체: 생략, 예매 진행 중: ONGOING, 공연 종료: ENDED)", required = false)
            @RequestParam(required = false) AmateurShowStatus status
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Slice<AmateurShowResponseDTO.MyShowAmateurShowList> slice = amateurService.getMyAmateurShow(member.getId(), status, pageable);
        return ApiResponse.onSuccess(SliceResponse.of(slice));
    }

    @PreAuthorize("hasRole('PERFORMER')")
    @GetMapping("/myPage/reserveList")
    @Operation(summary = "공연진 예매 내역 조회 첫화면", description = "등록자 계정으로 예매내역 조회를 누를시 나오는 첫화면입니다.")
    public ApiResponse<SliceResponse<AmateurShowResponseDTO.MyShowAmateurShowList>> getReserveList(
            @Parameter(description = "작성자 회원 ID", required = true)
            @AuthenticationPrincipal(expression = "member") Member member,
            @Parameter(description = "페이지 번호(0부터 시작)", required = true)
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", required = true)
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "공연 상태 필터 (전체: 생략, 예매 진행 중: ONGOING, 공연 종료: ENDED)", required = false)
            @RequestParam(required = false) AmateurShowStatus status
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Slice<AmateurShowResponseDTO.MyShowAmateurShowList> slice = amateurService.getMyAmateurShow(member.getId(), status, pageable);
        return ApiResponse.onSuccess(SliceResponse.of(slice));
    }

    @PreAuthorize("hasRole('PERFORMER')")
    @GetMapping("/{amateurShowId}")
    @Operation(
            summary = "공연진 특정 공연 예매 내역 조회(회차별 요약 + 선택 회차 상세)",
            description = "등록자 계정으로 특정 공연의 예매 내역을 조회합니다. roundId가 있으면 해당 회차 상세, 없으면 첫 회차 상세를 반환합니다."
    )
    public ApiResponse<ShowReservationResponseDTO> getShowReservation(
            @PathVariable Long amateurShowId,
            @Parameter(description = "선택 회차 ID", example = "10")
            @RequestParam(required = false) Long roundId
    ) {
        return ApiResponse.onSuccess(
                performerService.getShowReservationList(amateurShowId, roundId)
        );
    }


}
