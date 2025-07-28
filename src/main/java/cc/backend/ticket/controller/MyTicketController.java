package cc.backend.ticket.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.member.entity.Member;
import cc.backend.ticket.dto.response.MemberTicketListResponseDTO;
import cc.backend.ticket.dto.response.MemberTicketResponseDTO;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.service.MemberTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "내 공연 티켓")
@RestController
@RequiredArgsConstructor
@RequestMapping("/myTickets")
public class MyTicketController {
    private final MemberTicketService memberTicketService;

    @GetMapping("/list")
    @Operation(
            summary = "내 티켓 리스트 조회 API",
            description = "회원의 예약된 공연 티켓 목록을 조회합니다. 상태(status)에 따라 필터링할 수 있습니다.",
            parameters = {
                    @Parameter(name = "status", description = "티켓 상태 (예: ALL, RESERVED, CANCELLED)", required = false),
                    @Parameter(name = "member", hidden = true)
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "티켓 리스트 조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MemberTicketListResponseDTO.class)))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "유효하지 않은 status 파라미터",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "인증되지 않은 사용자",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    public ApiResponse<List<MemberTicketListResponseDTO>> getMyTicketList(
            @AuthenticationPrincipal(expression = "member") Member member,
            @RequestParam(defaultValue = "ALL") String status) {

        List<MemberTicketListResponseDTO> tickets = memberTicketService.getMyTicketList(member.getId(), status);
        return ApiResponse.onSuccess(tickets);
    }


    @GetMapping("{memberTicketId}/getMyTicket")
    @Operation(
            summary = "내 티켓 단건 조회 API",
            description = "회원이 예매한 특정 티켓(단건)의 상세 정보를 조회합니다.",
            parameters = {
                    @Parameter(name = "memberTicketId", description = "조회할 티켓 ID", required = true),
                    @Parameter(name = "member", hidden = true)
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "티켓 상세 조회 성공",
                            content = @Content(schema = @Schema(implementation = MemberTicketResponseDTO.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "해당 티켓이 존재하지 않거나 권한이 없습니다.",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "인증되지 않은 사용자",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    public ApiResponse<MemberTicketResponseDTO> getMyTicket(
            @PathVariable Long memberTicketId,
            @AuthenticationPrincipal(expression = "member") Member member) {

        MemberTicketResponseDTO myTicket = memberTicketService.getMyTicket(member.getId(), memberTicketId);
        return ApiResponse.onSuccess(myTicket);
    }


    @PatchMapping("{memberTicketId}/cancel")
    @Operation(
            summary = "티켓 예약 취소하기 API",
            description = "회원이 예매한 티켓을 취소하는 기능입니다. 이미 취소된 티켓은 다시 취소할 수 없습니다.",
            parameters = {
                    @Parameter(name = "memberTicketId", description = "취소할 티켓 ID", required = true),
                    @Parameter(name = "member", hidden = true)
            },
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "티켓 예약 취소 성공",
                            content = @Content(schema = @Schema(implementation = MemberTicketResponseDTO.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "티켓을 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "이미 취소된 티켓입니다.",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "인증되지 않은 사용자",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    public ApiResponse<MemberTicketResponseDTO> cancelTicket(
            @PathVariable Long memberTicketId,
            @AuthenticationPrincipal(expression = "member") Member member) {

        MemberTicketResponseDTO myTicket = memberTicketService.cancelTicket(member.getId(), memberTicketId);
        return ApiResponse.onSuccess(myTicket);
    }



}
