package cc.backend.notice.repository;

import cc.backend.notice.entity.MemberNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberNoticeRepository extends JpaRepository<MemberNotice, Long> {
    List<MemberNotice> findAllByMemberId(Long memberId);
}
