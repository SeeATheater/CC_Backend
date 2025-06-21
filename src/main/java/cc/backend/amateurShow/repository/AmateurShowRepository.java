package cc.backend.amateurShow.repository;

import cc.backend.amateurShow.entity.AmateurShow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmateurShowRepository extends JpaRepository<AmateurShow, Long> {
    List<AmateurShow> findAllByMemberId(Long memberId);

    @Query("SELECT a FROM AmateurShow a WHERE a.id = :id")
    Optional<AmateurShow> findByIdWithDetails(@Param("id") Long id);

}
