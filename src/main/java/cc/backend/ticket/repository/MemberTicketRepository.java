package cc.backend.ticket.repository;

import cc.backend.ticket.entity.MemberTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberTicketRepository extends JpaRepository<MemberTicket, Long> {

    List<MemberTicket> findAllByMemberId(Long memberId);
    List<MemberTicket> findAllByMemberIdAndReservationStatus(Long memberId, ReservationStatus reservationStatus);
    Optional<MemberTicket> findByMemberIdAndId(Long memberId, Long ticketId);

    List<MemberTicket> findAllByAmateurRound_PerformanceDateTimeBetween(LocalDateTime start, LocalDateTime end);
    default List<MemberTicket> findAllByPerformanceDate(LocalDate date) {
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
    Optional<MemberTicket> findWithTicketAndShowById(Long id);
}
