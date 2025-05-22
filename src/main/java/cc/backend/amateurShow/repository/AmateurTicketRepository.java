package cc.backend.amateurShow.repository;

import cc.backend.amateurShow.entity.AmateurTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmateurTicketRepository extends JpaRepository<AmateurTicket, Long> {

    List<AmateurTicket> findByAmateurShowId(Long amateurShowId);

}
