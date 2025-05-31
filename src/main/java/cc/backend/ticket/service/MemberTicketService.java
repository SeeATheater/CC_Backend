package cc.backend.ticket.service;

import cc.backend.ticket.dto.response.MemberTicketListResponseDTO;
import cc.backend.ticket.dto.request.MemberTicketCreateRequestDTO;
import cc.backend.ticket.dto.response.MemberTicketCreateResponseDTO;
import cc.backend.ticket.dto.response.MemberTicketResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MemberTicketService {

    MemberTicketCreateResponseDTO create(Long amateurTicketID, MemberTicketCreateRequestDTO requestDTO);
    List<MemberTicketListResponseDTO> getMyTicketList(Long memberId, String status);
    MemberTicketResponseDTO getMyTicket(Long memberId, Long ticketId);
    MemberTicketResponseDTO cancelTicket(Long memberId, Long ticketId);



}
