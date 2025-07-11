package cc.backend.ticket.service;

import cc.backend.member.entity.Member;
import cc.backend.ticket.dto.response.MemberTicketListResponseDTO;
import cc.backend.ticket.dto.request.MemberTicketCreateRequestDTO;
import cc.backend.ticket.dto.response.MemberTicketCreateResponseDTO;
import cc.backend.ticket.dto.response.MemberTicketResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MemberTicketService {

    MemberTicketCreateResponseDTO createTicket(Long amateurRoundId, Long amateurTicketId, Long memberId, MemberTicketCreateRequestDTO requestDTO);
    List<MemberTicketListResponseDTO> getMyTicketList(Long memberId, String status);
    MemberTicketResponseDTO getMyTicket(Long memberId, Long memberTicketId);
    MemberTicketResponseDTO cancelTicket(Long memberId, Long memberTicketId);



}
