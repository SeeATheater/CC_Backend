package cc.backend.performer;

import cc.backend.amateurShow.entity.AmateurShowStatus;
import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.performer.dto.PerformerMyShowResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공연진 마이페이지")
@RestController
@RequiredArgsConstructor
@RequestMapping("/performers")
public class PerformerController {

    private final PerformerService performerService;

    @GetMapping("/my-shows")
    @Operation(summary = "내가 등록한 공연 목록", description = "탭 필터: all(전체) / on_sale(예매 진행) / ended(공연 종료)")
    public ApiResponse<Slice<PerformerMyShowResponseDTO>> getMyShows(
            @AuthenticationPrincipal(expression = "member") Long memberId,
            @Parameter(description = "탭바 선택, all, on_sale, ended ", required = true) @RequestParam String tab,
            @Parameter(description = "페이지(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.onSuccess(performerService.getMyShows(memberId, tab, pageable));
    }
}
