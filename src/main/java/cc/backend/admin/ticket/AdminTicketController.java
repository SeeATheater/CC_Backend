package cc.backend.admin.ticket;

import cc.backend.admin.ticket.dto.ReservationListResponseDTO;
import cc.backend.apiPayLoad.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "관리자 소극장 티켓 관리")
@RequestMapping("/admin/ticket")
public class AdminTicketController {

    private final AdminTicketService adminTicketService;

    @GetMapping("/reservation/history")
    @Operation(summary = "예매 내역 관리", description = "예매 내역을 리스트 형태로 조회합니다.")
    public ApiResponse<Slice<ReservationListResponseDTO>> getReservationHistory(
            @Parameter(description = "페이지 번호(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "검색 키워드, 공연 명으로 검색", example = "실종")
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.onSuccess(adminTicketService.getReservationList(page, size, keyword));
    }

//    @GetMapping("/reservation/{realTicketId}")
//    @Operation

}
