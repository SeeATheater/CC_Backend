package cc.backend.repository.amateurRepository;

import cc.backend.domain.entity.amateur.AmateurStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmateurStaffRepository extends JpaRepository<AmateurStaff, Long> {
}
