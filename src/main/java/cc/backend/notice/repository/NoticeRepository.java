package cc.backend.notice.repository;

import cc.backend.notice.entity.Notice;
import cc.backend.notice.entity.enums.NoticeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Boolean existsByContentIdAndType(Long contentId, NoticeType type);
}
