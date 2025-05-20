package cc.backend.amateurShow.repository;

import cc.backend.amateurShow.entity.AmateurSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmateurSummaryRepository extends JpaRepository<AmateurSummary, Long> {
}
