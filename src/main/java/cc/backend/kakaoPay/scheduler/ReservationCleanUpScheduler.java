package cc.backend.kakaoPay.scheduler;

import cc.backend.amateurShow.repository.AmateurRoundsRepository;
import cc.backend.ticket.entity.MemberTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import cc.backend.ticket.repository.MemberTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCleanUpScheduler {
    // 예약 상태가 PENDING인 티켓을 주기적으로 정리하는 스케줄러
    // 15분 이상 PENDING 상태인 티켓은 자동으로 EXPIRED로 변경

    private final MemberTicketRepository memberTicketRepository;
    private final AmateurRoundsRepository amateurRoundsRepository;

    // 5분마다 실행
    @Scheduled(fixedRate = 300000) // 5분 = 300000 milliseconds
    @Transactional
    public void cleanUpPendingReservations() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);

        List<MemberTicket> pendingTickets = memberTicketRepository.findByReservationStatusAndCreatedAtBefore(ReservationStatus.PENDING, expirationTime);

        if (pendingTickets.isEmpty()) {
            log.info("No pending reservations to clean up.");
            return;
        }

        for (MemberTicket ticket : pendingTickets) {

            // 상태 다시 한 번 점검
            if (!ticket.getReservationStatus().equals(ReservationStatus.PENDING)) {
                log.warn("ticketId={} is not in PENDING status, skipping cleanup.", ticket.getId());
                continue;
            }

            ticket.updateReservationStatus(ReservationStatus.EXPIRED); // 티켓 상태를 EXPIRED로 변경

            // 해당 티켓의 회차 재고 복구
            int updated = amateurRoundsRepository.increaseStock(ticket.getAmateurRound().getId(), ticket.getQuantity());

            if (updated == 0) {
                log.error("Failed to restore stock for ticketId={}, roundId={}, quantity={}",
                        ticket.getId(), ticket.getAmateurRound().getId(), ticket.getQuantity());
            } else {
                log.info("Expired + Restored stock for ticketId={}, roundId={}, quantity={}",
                        ticket.getId(), ticket.getAmateurRound().getId(), ticket.getQuantity());
            }
        }
    }
}
