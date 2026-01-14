package cc.backend.ticket.service;

import cc.backend.member.entity.Member;
import cc.backend.ticket.dto.response.*;
import cc.backend.ticket.dto.request.TempTicketCreateRequestDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TempTicketService {

    TempTicketCreateResponseDTO createTicket(Long amateurShowId, Long amateurRoundId, Long amateurTicketId, Member member, TempTicketCreateRequestDTO requestDTO);
    List<RoundsListDTO> getRoundsList(Long memberId, Long amateurShowId);
    List<AmateurTicketListDTO> getAmateurTicketList(Long memberId, Long amateurShowId);
    AmateurShowSimpleDTO getSimpleAmateurShow(Long amateurShowId);


}
