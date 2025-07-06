package cc.backend.kakaoPay.service;

import cc.backend.amateurShow.repository.AmateurRoundsRepository;
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
    private final AmateurRoundsRepository amateurRoundsRepository;

    // ready 단계에서 재고 선점
    public void preemptStock(Long ticketId) {

        MemberTicket memberTicket = memberTicketRepository.findById(ticketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));

        // 해당 티켓의 회차, 수량으로 재고 감소시키기
        int updated = amateurRoundsRepository.decreaseStock(memberTicket.getAmateurRound().getId(), memberTicket.getQuantity());

        if (updated == 0) { // 재고 부족하면 예외
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_STOCK);
        }
    }

    // approve 단계에서 그냥 상태 확정
    public void confirmReservation(String partnerOrderId) {

        Long ticketId = Long.valueOf(partnerOrderId);

        MemberTicket memberTicket = memberTicketRepository.findWithTicketAndShowById(ticketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));

        // 티켓이 이미 예매되었으면 중복 방지
        if (memberTicket.getReservationStatus().equals(ReservationStatus.RESERVED)) return;

        // PENDING 상태가 아니면 요청이 잘못된거임
        if (!memberTicket.getReservationStatus().equals(ReservationStatus.PENDING)) {
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_STATUS_INVALID);
        }

        memberTicket.updateReservationStatus(ReservationStatus.RESERVED); // 상태 변경
        memberTicket.getAmateurTicket().getAmateurShow().increaseSoldTicket(memberTicket.getQuantity()); // 누적 판매 티켓 수 증가
    }
}
