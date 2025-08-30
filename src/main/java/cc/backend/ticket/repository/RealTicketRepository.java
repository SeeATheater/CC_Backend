package cc.backend.ticket.repository;

import cc.backend.ticket.entity.RealTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RealTicketRepository extends JpaRepository<RealTicket, Long> {

    @Query("SELECT rt FROM RealTicket rt JOIN FETCH rt.amateurRound WHERE rt.id = :id AND rt.member.id = :memberId")
    Optional<RealTicket> findByIdAndMemberId(@Param("id") Long id, @Param("memberId") Long memberId);

    List<RealTicket> findAllByMemberId(Long memberId);

    List<RealTicket> findAllByMemberIdAndReservationStatus(Long memberId, ReservationStatus status);

    List<RealTicket> findByShowTitleAndReservationStatusInOrderByIdDesc(
            String showTitle,
            List<ReservationStatus> statuses
    );
    List<RealTicket> findByAmateurRound_IdOrderByReserveDateTimeAsc(Long roundId);

    @EntityGraph(attributePaths = {"member"})
    Page<RealTicket> findByShowTitleContainingIgnoreCase(String showTitle, Pageable pageable);

}
