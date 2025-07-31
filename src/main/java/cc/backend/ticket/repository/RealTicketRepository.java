package cc.backend.ticket.repository;

import cc.backend.ticket.entity.RealTicket;
import cc.backend.ticket.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RealTicketRepository extends JpaRepository<RealTicket, Long> {
    Optional<RealTicket> findByIdAndMemberId(Long id, Long memberId);


    List<RealTicket> findAllByMemberId(Long memberId);

    List<RealTicket> findAllByMemberIdAndReservationStatus(Long memberId, ReservationStatus status);


}
