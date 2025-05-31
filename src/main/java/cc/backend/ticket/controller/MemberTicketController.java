package cc.backend.ticket.controller;

import cc.backend.apiPayLoad.ApiResponse;
import cc.backend.member.MemberService;
import cc.backend.ticket.dto.MemberTicketListResponseDTO;
import cc.backend.ticket.dto.request.MemberTicketCreateRequestDTO;
import cc.backend.ticket.dto.response.MemberTicketCreateResponseDTO;
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
    public ApiResponse<List<MemberTicketListResponseDTO>> getMyTickets(
            @RequestParam Long memberId,
            @RequestParam(defaultValue = "ALL") String status) {

        List<MemberTicketListResponseDTO> tickets = memberTicketService.getMyTickets(memberId, status);
        return ApiResponse.onSuccess(tickets);
    }
}
