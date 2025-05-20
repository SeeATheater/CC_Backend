package cc.backend.amateurShow.repository;

import cc.backend.amateurShow.entity.AmateurShow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AmateurShowRepository extends JpaRepository<AmateurShow, Long> {
    List<AmateurShow> findAllByMemberId(Long memberId);

}
