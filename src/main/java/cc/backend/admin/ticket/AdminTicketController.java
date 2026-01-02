package cc.backend.admin.ticket;

import cc.backend.admin.ticket.dto.RefundDetailResponseDTO;
import cc.backend.admin.ticket.dto.RefundListResponseDTO;
import cc.backend.admin.ticket.dto.ReservationDetailResponseDTO;
import cc.backend.admin.ticket.dto.TicketDetailResponseDTO;
import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.apiPayLoad.SliceResponse;
import com.google.protobuf.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "관리자 소극장 티켓 관리")
@RequestMapping("/admin/ticket")
public class AdminTicketController {

    private final AdminTicketService adminTicketService;

    @GetMapping("/history")
    @Operation(summary = "관리자 소극장 티켓 관리 - 표")
    public ApiResponse<Slice<TicketDetailResponseDTO>> getTicketHistory(
            @Parameter(description = "페이지 번호(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "검색 키워드, 공연 명으로 검색", example = "실종")
            @RequestParam(required = false) String keyword
    ){
        return ApiResponse.onSuccess(adminTicketService.getTicketList(page, size, keyword));
    }

    @GetMapping("/{realTicketId}")
    @Operation(summary = "관리자 소극장 티켓 관리 상세조회")
    public ApiResponse<TicketDetailResponseDTO> getTicketDetail(
            @Parameter(description = "티켓 id", example = "1")
            @PathVariable Long realTicketId
    ){
        return ApiResponse.onSuccess(adminTicketService.getTicketDetail(realTicketId));
    }

    @GetMapping("/reservation/history")
    @Operation(summary = "관리자 예약 내역 관리 - 표", description = "예매 내역을 리스트 형태로 조회합니다.")
    public ApiResponse<SliceResponse<ReservationDetailResponseDTO>> getReservationHistory(
            @Parameter(description = "페이지 번호(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "검색 키워드, 공연 명으로 검색", example = "실종")
            @RequestParam(required = false) String keyword
    ) {
        Slice<ReservationDetailResponseDTO> slice = adminTicketService.getReservationList(page, size, keyword);
        return ApiResponse.onSuccess(SliceResponse.of(slice));    }

    @GetMapping("/reservation/{realTicketId}")
    @Operation(summary = "관리자 예약 내역 관리 상세조회")
    public ApiResponse<ReservationDetailResponseDTO> getReservationDetail(
            @Parameter(description = "티켓 id", example = "1")
            @PathVariable Long realTicketId
    ){
        return ApiResponse.onSuccess(adminTicketService.getReservationDetail(realTicketId));
    }

    @GetMapping("/refund/history")
    @Operation(summary = "관리자 환불 내역 관리 - 표", description = "환불 내역을 리스트 형태로 조회합니다.")
    public ApiResponse<Slice<RefundListResponseDTO>> getRefundHistory(
            @Parameter(description = "페이지 번호(0부터)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "검색 키워드, 공연 명으로 검색", example = "실종")
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.onSuccess(adminTicketService.getRefundList(page, size, keyword));
    }

    @GetMapping("/refund/{realTicketId}")
    @Operation(summary = "관리자 환불 내역 관리 상세조회")
    public ApiResponse<RefundDetailResponseDTO> getRefundDetail(
            @Parameter(description = "티켓 id", example = "1")
            @PathVariable Long realTicketId
    ){
        return ApiResponse.onSuccess(adminTicketService.getRefundDetail(realTicketId));
    }



}
