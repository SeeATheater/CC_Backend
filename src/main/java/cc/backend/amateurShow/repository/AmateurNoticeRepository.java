package cc.backend.amateurShow.repository;

import cc.backend.amateurShow.entity.AmateurNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmateurNoticeRepository extends JpaRepository<AmateurNotice, Long> {
}
