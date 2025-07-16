package cc.backend.amateurShow.repository;

import cc.backend.amateurShow.entity.AmateurRounds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface AmateurRoundsRepository extends JpaRepository<AmateurRounds, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AmateurRounds a SET a.totalTicket = a.totalTicket - :quantity WHERE a.id = :id AND a.totalTicket >= :quantity")
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    // 재고 증가 메소드
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AmateurRounds a SET a.totalTicket = a.totalTicket + :quantity WHERE a.id = :id")
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

}
