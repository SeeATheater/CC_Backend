package cc.backend.ticket.service;

import cc.backend.ticket.dto.MemberTicketListResponseDTO;
import cc.backend.ticket.dto.request.MemberTicketCreateRequestDTO;
import cc.backend.ticket.dto.response.MemberTicketCreateResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MemberTicketService {

    MemberTicketCreateResponseDTO create(Long amateurTicketID, MemberTicketCreateRequestDTO requestDTO);
    List<MemberTicketListResponseDTO> getMyTickets(Long memberId, String status);


}
