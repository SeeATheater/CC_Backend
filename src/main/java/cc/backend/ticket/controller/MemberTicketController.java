package cc.backend.ticket.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.member.MemberService;
import cc.backend.member.entity.Member;
import cc.backend.ticket.dto.response.*;
import cc.backend.ticket.dto.request.MemberTicketCreateRequestDTO;
import cc.backend.ticket.service.MemberTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "소극장 공연 티켓")
@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
public class MemberTicketController {

    private final MemberTicketService memberTicketService;

    @GetMapping("{amateurShowId}/showSimple")
    @Operation(summary = "소극장 공연 티켓 예매 - 공연 정보 간략 보기 API")
    public ApiResponse<AmateurShowSimpleDTO> getSimpleAmateurShow(@PathVariable Long amateurShowId,
                                                                  @AuthenticationPrincipal(expression = "member") Member member) {
        return ApiResponse.onSuccess(memberTicketService.getSimpleAmateurShow(amateurShowId));
    }

    @GetMapping("{amateurShowId}/selectRound")
    @Operation(summary = "소극장 공연 티켓 예매 첫화면 - 회차(날짜) 선택 API")
    public ApiResponse<List<RoundsListDTO>> getAmateurRounds(@PathVariable Long amateurShowId,
                                                             @AuthenticationPrincipal(expression = "member") Member member){

        return ApiResponse.onSuccess(memberTicketService.getRoundsList(member.getId(), amateurShowId));

    }

    @GetMapping("{amateurShowId}/selectTicket")
    @Operation(summary = "소극장 공연 티켓 예매 두번재 화면 - 티켓 종류 선택 API")
    public ApiResponse<List<AmateurTicketListDTO>> getAmateurTicketList(@PathVariable Long amateurShowId,
                                                                    @AuthenticationPrincipal(expression = "member") Member member){

        return ApiResponse.onSuccess(memberTicketService.getAmateurTicketList(member.getId(), amateurShowId));

    }

    @PostMapping("{amateurShowId}/reserve")
    @Operation(summary = "소극장 공연 티켓 생성 API")
    public ApiResponse<MemberTicketCreateResponseDTO> createMemberTicket(
            @PathVariable Long amateurShowId,
            @RequestParam Long amateurRoundId,
            @RequestParam Long amateurTicketId,
            @AuthenticationPrincipal(expression = "member") Member member,
            @RequestBody MemberTicketCreateRequestDTO requestDTO) {
        return ApiResponse.onSuccess(
                memberTicketService.createTicket(amateurShowId, amateurRoundId, amateurTicketId, member, requestDTO)
        );
    }

    @GetMapping("/list")
    @Operation(summary = "내 티켓 리스트 조회 API")
    public ApiResponse<List<MemberTicketListResponseDTO>> getMyTicketList(
            @AuthenticationPrincipal(expression = "member") Member member,
            @RequestParam(defaultValue = "ALL") String status) {

        List<MemberTicketListResponseDTO> tickets = memberTicketService.getMyTicketList(member.getId(), status);
        return ApiResponse.onSuccess(tickets);
    }

    @GetMapping("{memberTicketId}/getMyTicket")
    @Operation(summary = "내 티켓 단건 조회 API")
    public ApiResponse<MemberTicketResponseDTO> getMyTicket(@PathVariable Long memberTicketId,
                                                            @AuthenticationPrincipal(expression = "member") Member member) {

        MemberTicketResponseDTO myTicket = memberTicketService.getMyTicket(member.getId(), memberTicketId);
        return ApiResponse.onSuccess(myTicket);
    }

    @PatchMapping("{memberTicketId}/cancel")
    @Operation(summary = "티켓 예약 취소하기 API")
    public ApiResponse<MemberTicketResponseDTO> cancelTicket(@PathVariable Long memberTicketId,
                                                             @AuthenticationPrincipal(expression = "member") Member member) {
        MemberTicketResponseDTO myTicket = memberTicketService.cancelTicket(member.getId(), memberTicketId);
        return ApiResponse.onSuccess(myTicket);
    }
}
