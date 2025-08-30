package cc.backend.amateurShow.repository;

import cc.backend.amateurShow.entity.AmateurRounds;
import cc.backend.amateurShow.entity.AmateurShow;
import cc.backend.amateurShow.entity.AmateurShowStatus;
import cc.backend.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AmateurShowRepository extends JpaRepository<AmateurShow, Long> {
    List<AmateurShow> findAllByMemberId(Long memberId);

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

    List<AmateurShow> findAllByMemberIdOrderByUpdatedAtDesc(@Param("memberId") Long memberId);


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




}