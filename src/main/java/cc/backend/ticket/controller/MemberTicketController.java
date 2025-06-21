package cc.backend.ticket.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.member.MemberService;
import cc.backend.ticket.dto.response.MemberTicketListResponseDTO;
import cc.backend.ticket.dto.request.MemberTicketCreateRequestDTO;
import cc.backend.ticket.dto.response.MemberTicketCreateResponseDTO;
import cc.backend.ticket.dto.response.MemberTicketResponseDTO;
import cc.backend.ticket.service.MemberTicketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "소극장 공연 티켓")
@RestController
@RequiredArgsConstructor
@RequestMapping("/tickets")
public class MemberTicketController {

    private final MemberService memberService;
    private final MemberTicketService memberTicketService;

    @PostMapping("/{amateurTicketId}")
    public ApiResponse<MemberTicketCreateResponseDTO> createMemberTicket(@PathVariable Long amateurTicketId,
                                                                         @RequestBody MemberTicketCreateRequestDTO requestDTO) {
        return ApiResponse.onSuccess(memberTicketService.create(amateurTicketId, requestDTO));

    }

    @GetMapping
    public ApiResponse<List<MemberTicketListResponseDTO>> getMyTicketList(
            @RequestParam Long memberId,
            @RequestParam(defaultValue = "ALL") String status) {

        List<MemberTicketListResponseDTO> tickets = memberTicketService.getMyTicketList(memberId, status);
        return ApiResponse.onSuccess(tickets);
    }

    @GetMapping("/{ticketId}")
    public ApiResponse<MemberTicketResponseDTO> getMyTicket(@PathVariable Long ticketId,
                                                                @RequestParam Long memberId) {

        MemberTicketResponseDTO myTicket = memberTicketService.getMyTicket(memberId, ticketId);
        return ApiResponse.onSuccess(myTicket);
    }

    @PatchMapping("/{ticketId}")
    public ApiResponse<MemberTicketResponseDTO> cancelTicket(@PathVariable Long ticketId,
                                                             @RequestParam Long memberId) {
        MemberTicketResponseDTO myTicket = memberTicketService.cancelTicket(memberId, ticketId);
        return ApiResponse.onSuccess(myTicket);
    }
}
