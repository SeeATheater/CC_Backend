package cc.backend.kakaoPay.service;

import cc.backend.amateurShow.entity.AmateurRounds;
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

    public void handleApprovedTicket(String partnerOrderId) {

        Long ticketId = Long.valueOf(partnerOrderId);

        // 일단 티켓으로 해당 회차만 알아내고
        Long roundId = memberTicketRepository.findAmateurRoundIdById(ticketId);
        if (roundId == null) {
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND);
        }

        // 그 roundId로 해당 회차 row에 먼저 락 걸기
        AmateurRounds amateurRounds = amateurRoundsRepository.findByIdWithLock(roundId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.ROUND_NOT_FOUND));

        // 이제 티켓 조회
        MemberTicket memberTicket = memberTicketRepository.findById(ticketId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_TICKET_NOT_FOUND));

        // 상태 확인
        if (!memberTicket.getReservationStatus().equals(ReservationStatus.PENDING)) {
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_ALREADY_RESERVED);
        }

        // 락이 걸린 그 회차 기준으로 재고 확인 (동시성 문제 해결)
        if (amateurRounds.getTotalTicket() < memberTicket.getQuantity()) {
            throw new GeneralException(ErrorStatus.MEMBER_TICKET_STOCK);
        }

        memberTicket.getAmateurRound().decreaseTotalTicket(memberTicket.getQuantity()); // 재고 감소
        memberTicket.updateReservationStatus(ReservationStatus.RESERVED); // 상태 변경
        memberTicket.getAmateurTicket().getAmateurShow().increaseSoldTicket(memberTicket.getQuantity()); // 누적 판매 티켓 수 증가
    }
}
