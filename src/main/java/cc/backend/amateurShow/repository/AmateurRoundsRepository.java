package cc.backend.amateurShow.repository;

import cc.backend.amateurShow.entity.AmateurRounds;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AmateurRoundsRepository extends JpaRepository<AmateurRounds, Long> {
    List<AmateurRounds> findByAmateurShow_IdOrderByRoundNumberAsc(Long amateurShowId);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE AmateurRounds a SET a.totalTicket = a.totalTicket - :quantity WHERE a.id = :id AND a.totalTicket >= :quantity")
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    // 재고 증가 메소드
    @Modifying(flushAutomatically = true)
    @Query("UPDATE AmateurRounds a SET a.totalTicket = a.totalTicket + :quantity WHERE a.id = :id")
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    List<AmateurRounds> findByAmateurShowId(Long amateurShowId);

    Optional<AmateurRounds> findByIdAndAmateurShow_Id(Long id, Long amateurShowId);
}
