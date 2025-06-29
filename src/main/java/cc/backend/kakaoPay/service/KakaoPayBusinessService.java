package cc.backend.kakaoPay.service;

import cc.backend.apiPayLoad.code.status.ErrorStatus;
import cc.backend.apiPayLoad.exception.GeneralException;
import cc.backend.ticket.entity.MemberTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.MemberTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class KakaoPayBusinessService {

    private final MemberTicketRepository memberTicketRepository;

    public void handleApprovedTicket(String tid, String partnerOrderId) {
        MemberTicket ticket = memberTicketRepository.findWithTicketAndShowById(Long.valueOf(partnerOrderId))
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));

        if (!ticket.getReservationStatus().equals(ReservationStatus.PENDING)) {
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_ALREADY_RESERVED);
        }

        ticket.updateTid(tid);
        ticket.updateReservationStatus(ReservationStatus.RESERVED);
        ticket.getAmateurRound().decreaseTotalTicket(ticket.getQuantity());
        ticket.getAmateurTicket().getAmateurShow().increaseSoldTicket(ticket.getQuantity());
    }
}



