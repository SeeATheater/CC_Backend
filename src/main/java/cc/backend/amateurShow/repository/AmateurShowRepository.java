package cc.backend.amateurShow.repository;

import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurShowStatus;
import cc.backend.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AmateurShowRepository extends JpaRepository<AmateurShow, Long>, JpaSpecificationExecutor<AmateurShow> {

    @Query("SELECT a FROM AmateurShow a WHERE a.id = :id")
    Optional<AmateurShow> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT a FROM AmateurShow a LEFT JOIN FETCH a.amateurRounds WHERE a.member = :member")
    List<AmateurShow> findAllByMemberWithRounds(@Param("member") Member member);

    @Query("SELECT a FROM AmateurShow a WHERE a.member.id = :memberId ORDER BY a.id DESC")
    Slice<AmateurShow> findAllByMemberIdOrderByIdDesc(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT a FROM AmateurShow a WHERE a.member.id = :memberId AND a.status = :status ORDER BY a.id DESC")
    Slice<AmateurShow> findAllByMemberIdAndStatusOrderByIdDesc(@Param("memberId") Long memberId,
                                                               @Param("status") AmateurShowStatus status,
                                                               Pageable pageable);

    @Query("""
       select s
         from AmateurShow s
        where lower(s.name) like lower(concat('%', :kw, '%'))
           or lower(s.performerName) like lower(concat('%', :kw, '%'))
        order by s.createdAt desc
       """)
    Slice<AmateurShow> findByNameOrPerformer(
            @Param("kw") String keyword,
            Pageable pageable
    );


    Slice<AmateurShow> findByMember_IdAndStatusInOrderByIdDesc(
            Long memberId,
            Collection<AmateurShowStatus> statuses,
            Pageable pageable
    );

    Slice<AmateurShow> findByMember_IdOrderByIdDesc(Long memberId, Pageable pageable);

    long countByMember_Id(Long memberId);

    Page<AmateurShow> findByNameContainingIgnoreCase(String showName, Pageable pageable);

    List<AmateurShow> findAllByMemberId(Long memberId);

    @Query("""
    SELECT s
    FROM AmateurShow s
    WHERE s.end >= :today
    AND s.approvalStatus = 'APPROVED'
    ORDER BY s.end ASC
""")
    List<AmateurShow> findHotShows(
            @Param("today") LocalDate today,
            Pageable pageable
    );

    @Query("select a from AmateurShow a")
    @EntityGraph(attributePaths = {"amateurRounds", "amateurNotice"}, type = EntityGraph.EntityGraphType.FETCH)
    List<AmateurShow> findAllWithRounds();

    List<AmateurShow> findByStatusIn(Collection<AmateurShowStatus> statuses);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE AmateurShow s SET s.status = :newStatus WHERE s.id IN :ids")
    void updateStatusByIds(@Param("ids") List<Long> ids, @Param("newStatus") AmateurShowStatus newStatus);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE AmateurShow s SET s.status = 'ONGOING' " +
            "WHERE s.status = 'YET' AND s.start <= :today AND s.end >= :today")
    int updateShowsToOngoing(@Param("today") LocalDate today);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE AmateurShow s SET s.status = 'ENDED' " +
            "WHERE s.status = 'ONGOING' AND s.end < :today")
    int updateShowsToEnded(@Param("today") LocalDate today);

    @Query("SELECT s.hashtag FROM AmateurShow s WHERE s.member.id = :memberId")
    List<String> findHashtagsByMemberId(@Param("memberId") Long memberId);
}