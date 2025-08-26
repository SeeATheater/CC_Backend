package cc.backend.notice.repository;

import cc.backend.notice.entity.MemberNotice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MemberNoticeRepository extends JpaRepository<MemberNotice, Long> {
    List<MemberNotice> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
    @Query("""
        SELECT mn
        FROM MemberNotice mn
        WHERE mn.member.id = :memberId
          AND (:cursorCreatedAt IS NULL 
            OR mn.createdAt < :cursorCreatedAt 
            OR (mn.createdAt = :cursorCreatedAt AND mn.id < :cursorId))
        ORDER BY mn.createdAt DESC, mn.id DESC
    """)
    List<MemberNotice> findMemberNoticeByCursor(
            @Param("memberId") Long memberId,
            @Param("cursorId") Long cursorId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            Pageable pageable
    );

}
