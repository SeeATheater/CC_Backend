package cc.backend.repository.amateurRepository;

import cc.backend.domain.entity.amateur.AmateurCasting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmateurCastingRepository extends JpaRepository<AmateurCasting, Long> {
}
