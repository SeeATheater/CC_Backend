package cc.backend.notice.repository;

import cc.backend.notice.entity.MemberNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberNoticeRepository extends JpaRepository<MemberNotice, Long> {
}
