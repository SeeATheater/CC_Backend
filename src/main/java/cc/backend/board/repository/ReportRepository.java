package cc.backend.board.repository;

import cc.backend.board.entity.Report;
import cc.backend.board.entity.enums.ReportTarget;
import cc.backend.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByReporterAndTargetTypeAndTargetId(Member reporter, ReportTarget targetType, Long targetId);
    long countByTargetTypeAndTargetId(ReportTarget targetType, Long targetId);
    List<Report> findByTargetTypeAndTargetId(ReportTarget targetType, Long targetId);
    List<Report> findByTargetType(ReportTarget targetType);
}
