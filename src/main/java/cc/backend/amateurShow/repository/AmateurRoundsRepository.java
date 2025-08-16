package cc.backend.amateurShow.repository;

import cc.backend.amateurShow.entity.AmateurRounds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmateurRoundsRepository extends JpaRepository<AmateurRounds, Long> {
    List<AmateurRounds> findByAmateurShowId(Long amateurShowId);
    List<AmateurRounds> findByAmateurShow_IdOrderByRoundNumberAsc(Long amateurShowId);

}
