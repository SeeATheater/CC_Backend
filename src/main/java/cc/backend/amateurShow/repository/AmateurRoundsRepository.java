package cc.backend.amateurShow.repository;

import cc.backend.amateurShow.entity.AmateurRounds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmateurRoundsRepository extends JpaRepository<AmateurRounds, Long> {
}
