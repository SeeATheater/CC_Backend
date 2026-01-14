package cc.backend.ticket.repository;

import cc.backend.ticket.entity.TempTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TempTicketRepository extends JpaRepository<TempTicket, Long> {

    List<TempTicket> findAllByMemberId(Long memberId);
    List<TempTicket> findAllByMemberIdAndReservationStatus(Long memberId, ReservationStatus reservationStatus);
    Optional<TempTicket> findByMemberIdAndId(Long memberId, Long ticketId);

    List<TempTicket> findAllByAmateurRound_PerformanceDateTimeBetween(LocalDateTime start, LocalDateTime end);
    default List<TempTicket> findAllByPerformanceDate(LocalDate date) {
        return findAllByAmateurRound_PerformanceDateTimeBetween(
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay().minusNanos(1)
        );
    }

    //  결제시 사용
    @EntityGraph(attributePaths = {
            "amateurTicket",
            "amateurTicket.amateurShow",
            "amateurRound"
    })
    Optional<TempTicket> findWithTicketAndShowById(Long id);

    // 스케줄러에서 사용 (15분 이상 PENDING 상태인 티켓을 EXPIRED로 변경)
    List<TempTicket> findByReservationStatusAndCreatedAtBefore(ReservationStatus status, LocalDateTime expirationTime);
}
