package cc.backend.ticket.repository;

import cc.backend.ticket.entity.RealTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    Slice<RealTicket> findAllByMemberId(Long memberId, Pageable pageable);

    Slice<RealTicket> findAllByMemberIdAndReservationStatus(Long memberId, ReservationStatus reservationStatus, Pageable pageable);

    List<RealTicket> findByShowTitleAndReservationStatusInOrderByIdDesc(
            String showTitle,
            List<ReservationStatus> statuses
    );
    List<RealTicket> findByAmateurRound_IdOrderByReserveDateTimeAsc(Long roundId);

    @EntityGraph(attributePaths = {"member"})
    Page<RealTicket> findByShowTitleContainingIgnoreCase(String showTitle, Pageable pageable);


    @EntityGraph(attributePaths = {"member"})
    Page<RealTicket> findByReservationStatus(ReservationStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"member"})
    Page<RealTicket> findByReservationStatusAndShowTitleContainingIgnoreCase(
            ReservationStatus status, String showTitle, Pageable pageable
    );

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RealTicket rt SET rt.reservationStatus = 'USED' " +
            "WHERE rt.reservationStatus = 'RESERVED' AND rt.performanceDateTime < :now")
    int updateReservedToUsed(@Param("now") LocalDateTime now);
}
