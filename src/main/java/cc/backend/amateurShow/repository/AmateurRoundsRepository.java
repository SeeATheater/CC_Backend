package cc.backend.amateurShow.repository;

import cc.backend.amateurShow.entity.AmateurRounds;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AmateurRoundsRepository extends JpaRepository<AmateurRounds, Long> {

    // 비관적 락 (결제 승인 트랜잭션이 수행되는 동안 해당 회차의 재고에 다른 트랜잭션이 접근하지 못하도록)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM AmateurRounds r WHERE r.id = :id")
    Optional<AmateurRounds> findByIdWithLock(@Param("id") Long id);
}
