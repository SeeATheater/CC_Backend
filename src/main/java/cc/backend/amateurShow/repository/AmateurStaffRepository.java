package cc.backend.amateurShow.repository;

import cc.backend.amateurShow.entity.AmateurStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmateurStaffRepository extends JpaRepository<AmateurStaff, Long> {
}
