package cc.backend.ticket.service;

import cc.backend.amateurShow.entity.AmateurTicket;
import cc.backend.member.entity.Member;
import cc.backend.ticket.dto.response.*;
import cc.backend.ticket.dto.request.MemberTicketCreateRequestDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MemberTicketService {

    MemberTicketCreateResponseDTO createTicket(Long amateurShowId, Long amateurRoundId, Long amateurTicketId, Member member, MemberTicketCreateRequestDTO requestDTO);
    List<MemberTicketListResponseDTO> getMyTicketList(Long memberId, String status);
    MemberTicketResponseDTO getMyTicket(Long memberId, Long memberTicketId);
    MemberTicketResponseDTO cancelTicket(Long memberId, Long memberTicketId);
    List<RoundsListDTO> getRoundsList(Long memberId, Long amateurShowId);
    List<AmateurTicketListDTO> getAmateurTicketList(Long memberId, Long amateurShowId);
    AmateurShowSimpleDTO getSimpleAmateurShow(Long amateurShowId);


}
