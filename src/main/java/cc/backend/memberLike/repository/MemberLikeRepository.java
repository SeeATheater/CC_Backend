package cc.backend.memberLike.repository;

import cc.backend.member.entity.Member;
import cc.backend.memberLike.entity.MemberLike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberLikeRepository extends JpaRepository<MemberLike, Long> {
    boolean existsByLikerAndPerformer(Member liker, Member performer);

    Optional<MemberLike> findByLikerAndPerformer(Member liker, Member performer);

    List<MemberLike> findByPerformerId(Long performerId);

    @Query("""
    SELECT l
    FROM MemberLike l
    JOIN l.performer p
    LEFT JOIN AmateurShow s ON s.member = p
    LEFT JOIN s.amateurRounds r
        ON r.performanceDateTime > :now
    WHERE l.liker = :member
    GROUP BY l
    ORDER BY
        CASE WHEN MIN(r.performanceDateTime) IS NULL THEN 1 ELSE 0 END,
        MIN(r.performanceDateTime) ASC
""")
    Slice<MemberLike> findLikedPerformersSortedBySoonestShow(
            @Param("member") Member member,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    // memberId 기준으로 해당 멤버가 좋아요한 모든 공연자 조회
    @Query("SELECT ml FROM MemberLike ml WHERE ml.liker.id = :memberId")
    List<MemberLike> findByLikerId(@Param("memberId") Long memberId);

    // 모든 회원을 대상으로, 추천 검사를 위해 distinct Member만 가져오기
    @Query("SELECT DISTINCT ml.liker FROM MemberLike ml")
    List<Member> findAllDistinctMembers();
}
