package cc.backend.amateurShow.repository;

import cc.backend.amateurShow.entity.AmateurCasting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmateurCastingRepository extends JpaRepository<AmateurCasting, Long> {
}
