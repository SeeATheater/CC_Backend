package cc.backend.ticket.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.member.MemberService;
import cc.backend.member.entity.Member;
import cc.backend.ticket.dto.response.MemberTicketListResponseDTO;
import cc.backend.ticket.dto.request.MemberTicketCreateRequestDTO;
import cc.backend.ticket.dto.response.MemberTicketCreateResponseDTO;
import cc.backend.ticket.dto.response.MemberTicketResponseDTO;
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
public class MemberTicketController {

    private final MemberService memberService;
    private final MemberTicketService memberTicketService;

    @PostMapping("/rounds/{amateurRoundId}/tickets/{amateurTicketId}")
    @Operation(summary = "소극장 공연 티켓 생성 API")
    public ApiResponse<MemberTicketCreateResponseDTO> createMemberTicket(@PathVariable Long amateurRoundId,
                                                                         @PathVariable Long amateurTicketId,
                                                                         @AuthenticationPrincipal(expression = "member") Member member,
                                                                         @RequestBody MemberTicketCreateRequestDTO requestDTO) {
        return ApiResponse.onSuccess(memberTicketService.createTicket(amateurRoundId, amateurTicketId, member.getId(), requestDTO));

    }

    @GetMapping("/tickets")
    @Operation(summary = "내 티켓 리스트 조회 API")
    public ApiResponse<List<MemberTicketListResponseDTO>> getMyTicketList(
            @AuthenticationPrincipal(expression = "member") Member member,
            @RequestParam(defaultValue = "ALL") String status) {

        List<MemberTicketListResponseDTO> tickets = memberTicketService.getMyTicketList(member.getId(), status);
        return ApiResponse.onSuccess(tickets);
    }

    @GetMapping("tickets/{memberTicketId}")
    @Operation(summary = "내 티켓 단건 조회 API")
    public ApiResponse<MemberTicketResponseDTO> getMyTicket(@PathVariable Long memberTicketId,
                                                            @AuthenticationPrincipal(expression = "member") Member member) {

        MemberTicketResponseDTO myTicket = memberTicketService.getMyTicket(member.getId(), memberTicketId);
        return ApiResponse.onSuccess(myTicket);
    }

    @PatchMapping("tickets/{memberTicketId}")
    @Operation(summary = "티켓 예약 취소하기 API")
    public ApiResponse<MemberTicketResponseDTO> cancelTicket(@PathVariable Long memberTicketId,
                                                             @AuthenticationPrincipal(expression = "member") Member member) {
        MemberTicketResponseDTO myTicket = memberTicketService.cancelTicket(member.getId(), memberTicketId);
        return ApiResponse.onSuccess(myTicket);
    }
}
